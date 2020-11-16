package com.github.adamsousa.myweather.ui.base

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.NavHostFragment

open class BaseActivity : AppCompatActivity() {

    override fun onBackPressed() {
        val fragmentList = supportFragmentManager.fragments
        var handled = false
        for (fragment in fragmentList) {
            if (fragment is NavHostFragment) {
                handled = onBackPressedNavHost(fragment)
                if (handled) {
                    break
                }
            } else if (fragment is BaseFragment) {
                handled = fragment.onBackPressed()
                if (handled) {
                    break
                }
            }
        }
        if (!handled) {
            super.onBackPressed()
        }
    }

    private fun onBackPressedNavHost(navHostFragment: NavHostFragment): Boolean {
        var handled = false
        val navHostFragments: List<Fragment?> =
            navHostFragment.childFragmentManager.fragments
        for (navFragment in navHostFragments.reversed()) {
            if (navFragment is BaseFragment) {
                handled = navFragment.onBackPressed()
                if (handled) {
                    break
                }
            }
        }
        return handled
    }
}