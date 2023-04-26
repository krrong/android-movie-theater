package woowacourse.movie.presentation.activities.main

import android.Manifest
import android.content.pm.PackageManager
import android.os.Build
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.fragment.app.commit
import com.google.android.material.bottomnavigation.BottomNavigationView
import woowacourse.movie.R
import woowacourse.movie.presentation.activities.main.fragments.history.HistoryFragment
import woowacourse.movie.presentation.activities.main.fragments.home.HomeFragment
import woowacourse.movie.presentation.activities.main.fragments.setting.SettingFragment

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val bottomNavigationView = findViewById<BottomNavigationView>(R.id.bottom_navigation_view)
        bottomNavigationView.selectedItemId = R.id.home

        bottomNavigationView.setOnItemSelectedListener { menu ->
            when (menu.itemId) {
                R.id.menu -> {
                    supportFragmentManager.commit {
                        replace(R.id.fragment_container_view, HistoryFragment.newInstance())
                    }
                }

                R.id.home -> {
                    supportFragmentManager.commit {
                        replace(R.id.fragment_container_view, HomeFragment.newInstance())
                    }
                }

                R.id.setting -> {
                    supportFragmentManager.commit {
                        replace(R.id.fragment_container_view, SettingFragment.newInstance())
                    }
                }
            }
            return@setOnItemSelectedListener true
        }

        requestNotificationPermission()
    }

    private fun requestNotificationPermission() {
        if (ContextCompat.checkSelfPermission(
                this, Manifest.permission.POST_NOTIFICATIONS
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                } else {
                    requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS)
                }
            } else {
                // 안드로이드 12 이하는 Notification에 관한 권한 필요 없음
            }
        }
    }

    private val requestPermissionLauncher = registerForActivityResult(
        ActivityResultContracts.RequestPermission()
    ) { isGranted: Boolean ->
        if (isGranted) {
            Toast.makeText(this, getString(R.string.permission_allowed), Toast.LENGTH_SHORT).show()
        }
    }
}
