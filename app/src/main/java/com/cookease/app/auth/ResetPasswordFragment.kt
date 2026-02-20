package com.cookease.app.ui.auth

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import com.cookease.app.R
import com.cookease.app.databinding.FragmentResetPasswordBinding

class ResetPasswordFragment : Fragment() {

    private var _binding: FragmentResetPasswordBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ResetPasswordViewModel by viewModels {
        ResetPasswordViewModelFactory(requireContext())
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentResetPasswordBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupListeners()
        setupObservers()
    }

    private fun setupListeners() {
        binding.etPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val pwd = s?.toString() ?: ""
                viewModel.calculateStrength(pwd)
                binding.strengthContainer.isVisible = pwd.isNotEmpty()
                val confirm = binding.etConfirmPassword.text?.toString() ?: ""
                binding.tvPasswordMismatch.isVisible = confirm.isNotEmpty() && pwd != confirm
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.etConfirmPassword.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                val pwd = binding.etPassword.text?.toString() ?: ""
                binding.tvPasswordMismatch.isVisible =
                    s?.toString() != pwd && s?.isNotEmpty() == true
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        binding.btnResetPassword.setOnClickListener {
            val pwd = binding.etPassword.text?.toString() ?: ""
            val confirm = binding.etConfirmPassword.text?.toString() ?: ""
            viewModel.validateAndReset(pwd, confirm)
        }

        binding.tvLoginLink.setOnClickListener {
            findNavController().navigate(R.id.action_resetPasswordFragment_to_loginFragment)
        }

        binding.tvLogo.setOnClickListener {
            findNavController().navigate(R.id.action_global_homeFragment)
        }
    }

    private fun setupObservers() {
        viewModel.passwordStrength.observe(viewLifecycleOwner) { score ->
            binding.strengthBar.progress = score
            binding.tvStrengthLabel.text = viewModel.getStrengthLabel(score)
            binding.tvStrengthLabel.setTextColor(
                resources.getColor(
                    when {
                        score <= 2 -> R.color.error
                        score <= 4 -> R.color.warning
                        else -> R.color.success
                    }, null
                )
            )
        }

        viewModel.state.observe(viewLifecycleOwner) { state ->
            binding.btnResetPassword.isEnabled = state !is ResetPasswordState.Loading
            binding.tvError.isVisible = false
            binding.tvSuccess.isVisible = false

            when (state) {
                is ResetPasswordState.Loading ->
                    binding.btnResetPassword.text = "Updating Password..."
                is ResetPasswordState.Success -> {
                    binding.tvSuccess.isVisible = true
                    binding.tvSuccess.text = "Password updated successfully! Redirecting..."
                    binding.btnResetPassword.text = "Reset Password"
                    binding.root.postDelayed({
                        findNavController().navigate(R.id.action_resetPasswordFragment_to_loginFragment)
                    }, 2000)
                }
                is ResetPasswordState.Error -> {
                    binding.tvError.isVisible = true
                    binding.tvError.text = state.message
                    binding.btnResetPassword.text = "Reset Password"
                }
                else -> binding.btnResetPassword.text = "Reset Password"
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}