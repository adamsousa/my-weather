package com.github.adamsousa.myweather.ui.base

import android.view.View
import androidx.fragment.app.Fragment
import com.github.adamsousa.myweather.R
import com.google.android.material.snackbar.Snackbar

open class BaseFragment : Fragment() {

    fun onBackPressed(): Boolean {
        return false
    }

    fun showAnchoredSnackBar(snackBar: Snackbar) {
        val bottomNav: View? = activity?.findViewById(R.id.bottom_nav)
        if (bottomNav != null) {
            snackBar.anchorView = bottomNav
        }
        snackBar.show()
    }
}