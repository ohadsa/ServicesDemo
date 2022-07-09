package gini.ohadsa.servicesdemo.location

import android.Manifest
import android.app.AlertDialog
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.ColorInt
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import gini.ohadsa.servicesdemo.utils.permissions.Permission
import com.kinandcarta.permissionmanager.permissions.PermissionRequestHandlerImpl
import dagger.hilt.android.AndroidEntryPoint
import gini.ohadsa.servicesdemo.databinding.FragmentLocationDemoBinding
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch
import javax.inject.Inject


@AndroidEntryPoint
class LocationFragment : Fragment() {

    private var _binding: FragmentLocationDemoBinding? = null
    private val binding get() = _binding!!
    private val viewModel: LocationDemoViewModel by activityViewModels()

    @Inject
    lateinit var permissionManager: PermissionRequestHandlerImpl


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentLocationDemoBinding.inflate(inflater, container, false)
        val root = binding.root


        permissionManager.from(this@LocationFragment)

        collectButtonTextFlow()

        collectFromLogResultFlow()

        initPermissionResultFlow()

        initStartServiceButtonListnener()

        return root
    }

    private fun collectButtonTextFlow() {
        lifecycleScope.launch {
            viewModel.buttonTextFlow.collectLatest { fromStrings ->
                binding.foregroundOnlyLocationButton.text = getString(fromStrings)
            }
        }
    }

    private fun initStartServiceButtonListnener() {
        binding.foregroundOnlyLocationButton.setOnClickListener {
            viewModel.serviceButtonPressed()
        }
    }

    private fun initPermissionResultFlow() {
        lifecycleScope.launch {
            viewModel.permissionRequesterFlow.collect { permissionData ->
                permissionManager
                    .request(Permission.Location)
                    .rationale(permissionData.rationale)
                    .checkDetailedPermission { result ->
                        if (result.all { it.value }) {
                            lifecycleScope.launch {
                                viewModel.permissionResultFlow.emit(true)
                            }
                        } else {
                            lifecycleScope.launch {
                                viewModel.permissionResultFlow.emit(false)
                            }
                        }
                    }
            }

        }
    }

    private fun collectFromLogResultFlow() {
        lifecycleScope.launch {
            viewModel.logResultToFragmentFlow.collectLatest { txt ->
                binding.outputTextView.text = txt
            }
        }
    }

}