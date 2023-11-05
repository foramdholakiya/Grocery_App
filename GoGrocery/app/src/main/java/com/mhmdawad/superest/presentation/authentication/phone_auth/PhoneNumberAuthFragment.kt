package com.mhmdawad.superest.presentation.authentication.phone_auth

import android.app.Dialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException
import com.google.firebase.auth.PhoneAuthOptions
import com.google.firebase.auth.PhoneAuthProvider
import com.mhmdawad.superest.R
import com.mhmdawad.superest.databinding.FragmentPhoneNumberAuthenticationBinding
import com.mhmdawad.superest.model.MainShopItem
import com.mhmdawad.superest.model.PhoneVerificationModel
import com.mhmdawad.superest.util.*
import com.mhmdawad.superest.util.extention.closeFragment
import com.mhmdawad.superest.util.extention.handleKeyBoardApparition
import com.mhmdawad.superest.util.extention.showToast
import com.mhmdawad.superest.util.extention.stopKeyBoardListener
import com.mhmdawad.superest.util.state.MainAuthState
import dagger.hilt.android.AndroidEntryPoint
import java.util.concurrent.TimeUnit
import javax.inject.Inject
import javax.inject.Named


@AndroidEntryPoint
class PhoneNumberAuthFragment : Fragment() {
    private lateinit var binding: FragmentPhoneNumberAuthenticationBinding

    @Inject
    lateinit var firebaseAuth: FirebaseAuth

    @Inject
    @Named(LOADING_ANNOTATION)
    lateinit var loadingDialog: Dialog

    private val authViewModel by viewModels<PhoneAuthViewModel>()

    private var verificationId: String? = null
    private var verificationToken: PhoneAuthProvider.ForceResendingToken? = null

    private var verificationTimeOut: Long = 0

    private var validPhoneNumber: String = ""

    private val selectedCountryCode by lazy {
        navArgs<PhoneNumberAuthFragmentArgs>().value.code.toInt()
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater, R.layout.fragment_phone_number_authentication, container, false
        )
        binding.fragment = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.apply {
            countryCodePicker.setCountryForPhoneCode(selectedCountryCode)
            backButton.setOnClickListener { closeFragment() }
        }
        observeListener()
    }

    private fun observeListener() {
        authViewModel.phoneMainAuthLiveData.observe(viewLifecycleOwner) {
            when (it) {
                /* some cases the phone number can be instantly verified without needing to send or enter a verification code
                    so here we will login with credential and we observe when login to open MainFragment if user has already an account
                    or open createUserFragment to add user info in app .
                 */
                is MainAuthState.SuccessWithCredential -> {
                    navigateToCreateUser()
                    loadingDialog.hide()
                }
                /*
                    Case two if user will verify with code has sent to him so here will open checkCodeAuth Fragment to check if
                    verification code has user added is correct .
                 */
                is MainAuthState.SuccessWithCode -> {
                    navigateToCreateUser()
                    loadingDialog.hide()

                }
                // If an error occurred will notify user with error message.
                is MainAuthState.Error -> {
                    loadingDialog.hide()
                    navigateToCreateUser()
                }
                // show loading dialog while send phone verification .
                is MainAuthState.Loading -> navigateToCreateUser()

                // hide loading dialog when app wait reaction from user.
                is MainAuthState.Idle -> navigateToCreateUser()

            }
        }

        authViewModel.signInStatusLiveData.observe(viewLifecycleOwner) {
            when (it) {
                // When had an error with automatically login app will push an error message.
                is Resource.Error -> {
                    loadingDialog.hide()
                    navigateToCreateUser()

                }
                /* Here we will login with credential and we observe when login to open MainFragment if user has already an account
                   or open createUserFragment to add user info in app .
                */
                is Resource.Success -> {
                    loadingDialog.hide()
                    navigateToCreateUser()
                }

                is Resource.Loading ->navigateToCreateUser()

            }
        }

    }
    fun checkPhoneNumber() {
        val phoneNumber = binding.phoneNumberEditText.text.toString().trim()
        val countryCode = binding.countryCodePicker.selectedCountryCode
        when {
            phoneNumber.isEmpty() -> {
                navigateToCreateUser()
            }

            phoneNumber.toInt() == 10 -> {
            navigateToCreateUser()
            }

            else -> {
                navigateToCreateUser()

            }
        }
    }
    private fun navigateToCreateUser() {
        val action = PhoneNumberAuthFragmentDirections.actionPhoneNumberAuthenticationFragmentToCreateUserInfoFragment()
        findNavController().navigate(action)
    }

    override fun onResume() {
        super.onResume()
        handleKeyBoardApparition(binding.authByPhoneNumberFAB)
    }

    override fun onStop() {
        super.onStop()
        stopKeyBoardListener()
    }
}