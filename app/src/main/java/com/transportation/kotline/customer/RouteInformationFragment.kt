package com.transportation.kotline.customer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import com.transportation.kotline.R
import com.transportation.kotline.customer.DetailRouteInformationFragment.Companion.TRAYEK_TYPE
import com.transportation.kotline.databinding.FragmentRouteInformationBinding

class RouteInformationFragment : Fragment(), View.OnClickListener {

    private var _binding: FragmentRouteInformationBinding? = null
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentRouteInformationBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.apply {
            cvAngkotA.setOnClickListener(this@RouteInformationFragment)
            cvAngkotB.setOnClickListener(this@RouteInformationFragment)
            cvAngkotC.setOnClickListener(this@RouteInformationFragment)
            cvAngkotD.setOnClickListener(this@RouteInformationFragment)
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        _binding = null
    }

    override fun onClick(v: View?) {
        when (v?.id) {
            R.id.cv_angkot_a -> {
                val mDetailRouteInformationFragment = DetailRouteInformationFragment()
                val mBundle = Bundle()
                mBundle.putString(TRAYEK_TYPE, "A")
                mDetailRouteInformationFragment.arguments = mBundle

                parentFragmentManager.commit {
                    replace(
                        R.id.fragment_container_view,
                        mDetailRouteInformationFragment,
                        DetailRouteInformationFragment::class.java.simpleName
                    )
                    addToBackStack(null)
                }
            }
            R.id.cv_angkot_b -> {
                val mDetailRouteInformationFragment = DetailRouteInformationFragment()
                val mBundle = Bundle()
                mBundle.putString(TRAYEK_TYPE, "B")
                mDetailRouteInformationFragment.arguments = mBundle

                parentFragmentManager.commit {
                    replace(
                        R.id.fragment_container_view,
                        mDetailRouteInformationFragment,
                        DetailRouteInformationFragment::class.java.simpleName
                    )
                    addToBackStack(null)
                }
            }
            R.id.cv_angkot_c -> {
                val mDetailRouteInformationFragment = DetailRouteInformationFragment()
                val mBundle = Bundle()
                mBundle.putString(TRAYEK_TYPE, "C")
                mDetailRouteInformationFragment.arguments = mBundle

                parentFragmentManager.commit {
                    replace(
                        R.id.fragment_container_view,
                        mDetailRouteInformationFragment,
                        DetailRouteInformationFragment::class.java.simpleName
                    )
                    addToBackStack(null)
                }
            }
            R.id.cv_angkot_d -> {
                val mDetailRouteInformationFragment = DetailRouteInformationFragment()
                val mBundle = Bundle()
                mBundle.putString(TRAYEK_TYPE, "D")
                mDetailRouteInformationFragment.arguments = mBundle

                parentFragmentManager.commit {
                    replace(
                        R.id.fragment_container_view,
                        mDetailRouteInformationFragment,
                        DetailRouteInformationFragment::class.java.simpleName
                    )
                    addToBackStack(null)
                }
            }
        }
    }
}