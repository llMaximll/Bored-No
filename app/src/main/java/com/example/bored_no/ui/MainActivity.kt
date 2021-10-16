package com.example.bored_no.ui

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.constraintlayout.motion.widget.MotionLayout
import androidx.constraintlayout.motion.widget.TransitionAdapter
import androidx.core.view.children
import androidx.lifecycle.ViewModelProvider
import com.example.bored_no.MainRepository
import com.example.bored_no.R
import com.example.bored_no.databinding.ActivityMainBinding
import com.example.bored_no.network.RetrofitService
import com.example.bored_no.utils.isNetworkAvailable
import com.google.android.material.chip.Chip
import com.google.android.material.dialog.MaterialAlertDialogBuilder


private const val TAG = "MainActivityTag"

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: MainViewModel
    private lateinit var binding: ActivityMainBinding

    private var isEnabledInteraction = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val retrofitService = RetrofitService.getInstance()
        val mainRepository = MainRepository(retrofitService)

        // ViewModelFactory нужен, чтобы поместить аргумент в MainViewModel
        viewModel = ViewModelProvider(this, MainViewModelFactory(mainRepository)).get(MainViewModel::class.java)

        viewModel.request.observe(this) { request ->
            if (request.key != 0) {
                binding.outputText.text = request.activity
            } else {
                binding.outputText.text = "No actions found, try changing the filter"
            }
            Log.i(TAG, "request = $request")

            binding.motionLayout.isInteractionEnabled = true
            isEnabledInteraction = true
        }

        viewModel.errorMessage.observe(this) { errorMessage ->
            Toast.makeText(this, errorMessage, Toast.LENGTH_SHORT).show()
        }

        viewModel.loading.observe(this) { loading ->
            isEnabledInteraction = if (loading) {
                binding.progressIndicator.setVisibilityExt(View.VISIBLE)
                false
            } else {
                binding.progressIndicator.setVisibilityExt(View.INVISIBLE)
                true
            }
        }

        setButtons()

        // Listeners
        binding.motionLayout.setTransitionListener(object : TransitionAdapter() {
            override fun onTransitionCompleted(motionLayout: MotionLayout?, currentId: Int) {
                super.onTransitionCompleted(motionLayout, currentId)

                when (currentId) {
                    R.id.offScreenLeft,
                    R.id.offScreenRight -> {
                        motionLayout?.progress = 0f
                        motionLayout?.setTransition(R.id.rest, R.id.offScreenRight)

                        binding.outputText.text = "Swipe left or right"
                        motionLayout?.isInteractionEnabled = false
                        if (isNetworkAvailable(this@MainActivity)) {
                            viewModel.getRandom()
                        } else {
                            binding.outputText.text =
                                "There is no connection. Please check the connection and restart the application."
                        }
                    }
                    R.id.rest -> {
                        binding.materialCardViewBottom.setVisibilityExt(View.VISIBLE)
                        binding.filterScrollView.setVisibilityExt(View.GONE)
                    }
                }
            }
        })

        binding.accessibilityCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.accessibilitySlider.isEnabled = true
            } else {
                binding.accessibilitySlider.isEnabled = false
                binding.accessibilitySlider.value = 1.0f
            }
        }

        binding.participantsCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.participantsSlider.isEnabled = true
            } else {
                binding.participantsSlider.isEnabled = false
                binding.participantsSlider.value = 1f
            }
        }

        binding.priceCheckBox.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                binding.priceSlider.isEnabled = true
            } else {
                binding.priceSlider.isEnabled = false
                binding.priceSlider.value = 0.0f
            }
        }
    }

    override fun onBackPressed() {
        if (checkParametersRequest()) {
            when (binding.motionLayout.currentState) {
                R.id.rest -> {
                    super.onBackPressed()
                }
                R.id.offScreenFilter -> {
                    binding.motionLayout.transitionToState(R.id.rest)
                }
            }
        } else {
            createDialogExitFilterScreen()
        }
    }

    private fun setButtons() {
        var leftSide = false

        binding.sendButton.setOnClickListener {
            if (isEnabledInteraction) {
                binding.motionLayout.transitionToState(
                    if (leftSide) R.id.offScreenLeft else R.id.offScreenRight
                )
                leftSide = !leftSide
                isEnabledInteraction = false
            }
        }

        binding.filterButton.setOnClickListener {
            if (binding.motionLayout.currentState == R.id.rest) {
                binding.materialCardViewBottom.setVisibilityExt(View.GONE)
                binding.filterScrollView.setVisibilityExt(View.VISIBLE)
                binding.motionLayout.transitionToState(R.id.offScreenFilter)
            } else {
                if (checkParametersRequest()) {
                    binding.motionLayout.transitionToState(R.id.rest)
                } else {
                    createDialogExitFilterScreen()
                }
            }
        }

        binding.applyButton.setOnClickListener {
            applyChanges()
        }

        binding.resetButton.setOnClickListener {
            viewModel.typeRequest = null
            binding.typeChipGroup.clearCheck()
            viewModel.accessibilityRequest = null
            binding.accessibilitySlider.value = 1.0f
            binding.accessibilityCheckBox.isChecked = false
            viewModel.participantsRequest = null
            binding.participantsSlider.value = 1f
            binding.participantsCheckBox.isChecked = false
            viewModel.priceRequest = null
            binding.priceSlider.value = 0.0f
            binding.priceCheckBox.isChecked = false
        }
    }

    private fun View.setVisibilityExt(visibility: Int) {
        val motionLayout = parent as MotionLayout
        motionLayout.constraintSetIds.forEach {
            val constraintSet = motionLayout.getConstraintSet(it) ?: return@forEach
            constraintSet.setVisibility(this.id, visibility)
            constraintSet.applyTo(motionLayout)
        }
    }

    /**
     * true - if everything matches, else - false
     */
    private fun checkParametersRequest(): Boolean {
        try {
            var typeRequest: String? = null
            val idChip = binding.typeChipGroup.checkedChipId
            if (idChip != View.NO_ID) {
                val chip: Chip = findViewById(idChip)
                typeRequest = chip.text.toString()
            }
            val accessibilityRequest: Float? = if (binding.accessibilityCheckBox.isChecked) binding.accessibilitySlider.value else null
            val participantsRequest: Int? = if (binding.participantsCheckBox.isChecked) binding.participantsSlider.value.toInt() else null
            val priceRequest: Float? = if (binding.priceCheckBox.isChecked) binding.priceSlider.value else null
            when {
                viewModel.typeRequest != typeRequest -> {
                    Log.i(TAG, "typeRequest")
                    return false
                }
                viewModel.accessibilityRequest != accessibilityRequest -> {
                    Log.i(TAG, "accessibilityRequest")
                    return false
                }
                viewModel.participantsRequest != participantsRequest -> {
                    Log.i(TAG, "participantsRequest")
                    return false
                }
                viewModel.priceRequest != priceRequest -> {
                    Log.i(TAG, "priceRequest")
                    return false
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }

        return true
    }

    private fun applyChanges() {
        try {
            // Type
            val idChip = binding.typeChipGroup.checkedChipId
            if (idChip != View.NO_ID) {
                val chip: Chip = findViewById(idChip)
                viewModel.typeRequest = chip.text.toString()
            } else {
                viewModel.typeRequest = null
            }
            // Accessibility
            if (binding.accessibilityCheckBox.isChecked) {
                val accessibilityCount = binding.accessibilitySlider.value
                viewModel.accessibilityRequest = accessibilityCount
            } else {
                viewModel.accessibilityRequest = null
            }
            // Participants
            if (binding.participantsCheckBox.isChecked) {
                val participantsCount = binding.participantsSlider.value
                viewModel.participantsRequest = participantsCount.toInt()
            } else {
                viewModel.participantsRequest = null
            }
            // Price
            if (binding.priceCheckBox.isChecked) {
                val priceCount = binding.priceSlider.value
                viewModel.priceRequest = priceCount
            } else {
                viewModel.priceRequest = null
            }

            binding.motionLayout.transitionToState(R.id.rest)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private fun cancelChanges() {
        // Type
        binding.typeChipGroup.children.forEach { chip ->
            if (viewModel.typeRequest != null) {
                (chip as Chip).isChecked = chip.text == viewModel.typeRequest
            } else {
                (chip as Chip).isChecked = false
            }
        }
        // Accessibility
        if (viewModel.accessibilityRequest != null) {
            binding.accessibilityCheckBox.isChecked = true
            binding.accessibilitySlider.value = viewModel.accessibilityRequest!!
        } else {
            binding.accessibilitySlider.value = 1.0f
            binding.accessibilityCheckBox.isChecked = false
        }
        // Participants
        if (viewModel.participantsRequest != null) {
            binding.participantsCheckBox.isChecked = true
            binding.participantsSlider.value = viewModel.participantsRequest!!.toFloat()
        } else {
            binding.participantsSlider.value = 1f
            binding.participantsCheckBox.isChecked = false
        }
        // Price
        if (viewModel.priceRequest != null) {
            binding.priceCheckBox.isChecked = true
            binding.priceSlider.value = viewModel.priceRequest!!
        } else {
            binding.priceSlider.value = 0.0f
            binding.priceCheckBox.isChecked = false
        }
    }

    private fun createDialogExitFilterScreen() {
        MaterialAlertDialogBuilder(this@MainActivity)
            .setTitle("Save changes?")
            .setMessage("When saving changes, other actions will be suggested")
            .setNeutralButton("Cancel") { dialog, which ->
                dialog.cancel()
            }
            .setNegativeButton("No") { dialog, which ->
                dialog.cancel()
                cancelChanges()
                binding.motionLayout.transitionToState(R.id.rest)
            }
            .setPositiveButton("Yes") { dialog, which ->
                dialog.cancel()
                applyChanges()
                binding.motionLayout.transitionToState(R.id.rest)
            }
            .show()
    }
}