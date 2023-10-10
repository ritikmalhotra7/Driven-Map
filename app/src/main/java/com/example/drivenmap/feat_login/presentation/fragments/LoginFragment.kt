package com.example.drivenmap.feat_login.presentation.fragments

import android.app.Activity
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.drivenmap.R
import com.example.drivenmap.databinding.FragmentLoginBinding
import com.example.drivenmap.feat_core.utils.Utils.USER_COLLECTION_NAME
import com.example.drivenmap.feat_map.domain.models.UserModel
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.firestore.FirebaseFirestore
import dagger.hilt.android.AndroidEntryPoint
import javax.inject.Inject

@AndroidEntryPoint
class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? = null
    private val binding by lazy {
        _binding!!
    }

    @Inject
    lateinit var mAuth: FirebaseAuth

    @Inject
    lateinit var fireStore: FirebaseFirestore

    private lateinit var googleSignInClient: GoogleSignInClient

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setViews()
    }

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
            if (result.resultCode == Activity.RESULT_OK) {
                val task = GoogleSignIn.getSignedInAccountFromIntent(result.data)
                task.getResult(ApiException::class.java)?.let { account ->
                    mAuth.signInWithCredential(
                        GoogleAuthProvider.getCredential(
                            account.idToken,
                            null
                        )
                    ).addOnSuccessListener {
                        val user = it.user
                        val doc = fireStore.collection(USER_COLLECTION_NAME).document(user!!.uid)
                        doc.set(
                            UserModel(
                                id = user.uid,
                                name = user.displayName ?: "Name not given",
                                phoneNumber = user.phoneNumber,
                                email = user.email,
                                profilePhoto = user.photoUrl.toString()
                            )
                        ).addOnSuccessListener {
                            findNavController().popBackStack()
                            findNavController().navigate(R.id.mapFragment)
                        }.addOnFailureListener {
                            Log.d("taget", it.toString())
                        }
                    }.addOnFailureListener {
                        Log.d("taget", it.toString())
                    }
                }
            } else {
                Toast.makeText(
                    requireContext(),
                    requireContext().getString(R.string.something_went_wrong_with_google_signing),
                    Toast.LENGTH_LONG
                ).show()
            }
        }

    private fun googleSignIn() {
        val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
            .requestIdToken(getString(R.string.default_web_client_id))
            .requestEmail()
            .build()
        googleSignInClient = GoogleSignIn.getClient(requireActivity(), gso)
        resultLauncher.launch(googleSignInClient.signInIntent)
    }

    private fun setViews() {

        binding.apply {
            fragmentLoginBtGoogle.setOnClickListener {
                googleSignIn()
            }
        }
    }
}