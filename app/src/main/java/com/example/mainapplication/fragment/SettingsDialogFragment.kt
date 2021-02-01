package com.example.mainapplication.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.DialogFragment
import com.example.mainapplication.R
import com.example.mainapplication.activities.MainActivity
import com.example.mainapplication.activities.sharedRepository
import kotlinx.android.synthetic.main.fragment_settings.view.*


class SettingsDialogFragment : DialogFragment() {

    private lateinit var root: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_settings, container, false)
        root.dialogToolbar.setNavigationOnClickListener {
            close()
            val activity = activity
            activity?.supportFragmentManager?.popBackStack()
        }
        return root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)

        val tempUnit = sharedRepository.getTempUnit()
        val lengthUnit = sharedRepository.getRainUnit()
        val speedUnit = sharedRepository.getSpeedUnit()
        val pressureUnit = sharedRepository.getPressureUnit()
        val refreshUnit = sharedRepository.getRefreshInt()

        when (tempUnit) {
            "°C" -> {
                root.unit_temp.text = getString(R.string.setting_unit_Celsuis)
            }
            "°F" -> {
                root.unit_temp.text = getString(R.string.setting_unit_Fahrenheit)
            }
            else -> {
                root.unit_temp.text = getString(R.string.setting_unit_Kelvin)
            }
        }

        if (lengthUnit == "mm") {
            root.unit_length.text = getString(R.string.setting_unit_mm)
        } else {
            root.unit_length.text = getString(R.string.setting_unit_in)
        }

        when (speedUnit) {
            "m/s" -> {
                root.unit_speed.text = getString(R.string.setting_unit_mps)
            }
            "kph" -> {
                root.unit_speed.text = getString(R.string.setting_unit_kph)
            }
            else -> {
                root.unit_speed.text = getString(R.string.setting_unit_mph)
            }
        }

        when (pressureUnit) {
            "hPa/mBar" -> {
                root.unit_pressure.text = getString(R.string.setting_unit_hpa)
            }
            "kPa" -> {
                root.unit_pressure.text = getString(R.string.setting_unit_kpa)
            }
            "mm Hg" -> {
                root.unit_pressure.text = getString(R.string.setting_unit_mmhg)
            }
            else -> {
                root.unit_pressure.text = getString(R.string.setting_unit_inhg)
            }
        }

        when (refreshUnit) {
            "0" -> {
                root.unit_refresh.text = getString(R.string.settings_none)
            }
            "15" -> {
                root.unit_refresh.text = getString(R.string.settings_15min)
            }
            "30" -> {
                root.unit_refresh.text = getString(R.string.settings_30min)
            }
            "1" -> {
                root.unit_refresh.text = getString(R.string.settings_1hour)
            }
            "2" -> {
                root.unit_refresh.text = getString(R.string.settings_2hour)
            }
            "6" -> {
                root.unit_refresh.text = getString(R.string.settings_6hour)
            }
            "12" -> {
                root.unit_refresh.text = getString(R.string.settings_12hour)
            }
            else -> {
                root.unit_refresh.text = getString(R.string.settings_24hour)
            }
        }

        root.layout_temp.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this.requireContext())
            alertDialog.setTitle(getString(R.string.setting_tempUnits))

            val tempArray = resources.getStringArray(R.array.temperatureUnits)

            alertDialog.setItems(
                tempArray
            ) { dialog, index ->
                when (index) {
                    0 -> {
                        sharedRepository.setTempUnit("°C")
                        root.unit_temp.text = getString(R.string.setting_unit_Celsuis)
                    }
                    1 -> {
                        sharedRepository.setTempUnit("°F")
                        root.unit_temp.text = getString(R.string.setting_unit_Fahrenheit)
                    }
                    2 -> {
                        sharedRepository.setTempUnit("K")
                        root.unit_temp.text = getString(R.string.setting_unit_Kelvin)
                    }
                }

                dialog.dismiss()
            }
            alertDialog.setNegativeButton(getString(R.string.dialog_cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            alertDialog.create()
            alertDialog.show()
        }

        root.layout_length.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this.requireContext())
            alertDialog.setTitle(getString(R.string.setting_lengthUnits))

            val tempArray = resources.getStringArray(R.array.lengthUnits)

            alertDialog.setItems(
                tempArray
            ) { dialog, index ->
                when (index) {
                    0 -> {
                        sharedRepository.setRainUnit("mm")
                        root.unit_length.text = getString(R.string.setting_unit_mm)
                    }
                    1 -> {
                        sharedRepository.setRainUnit("in")
                        root.unit_length.text = getString(R.string.setting_unit_in)
                    }
                }

                dialog.dismiss()
            }
            alertDialog.setNegativeButton(getString(R.string.dialog_cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            alertDialog.create()
            alertDialog.show()
        }

        root.layout_speed.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this.requireContext())
            alertDialog.setTitle(getString(R.string.setting_speedUnits))

            val tempArray = resources.getStringArray(R.array.speedUnits)

            alertDialog.setItems(
                tempArray
            ) { dialog, index ->
                when (index) {
                    0 -> {
                        sharedRepository.setSpeedUnit("m/s")
                        root.unit_speed.text = getString(R.string.setting_unit_mps)
                    }
                    1 -> {
                        sharedRepository.setSpeedUnit("kph")
                        root.unit_speed.text = getString(R.string.setting_unit_kph)
                    }
                    2 -> {
                        sharedRepository.setSpeedUnit("mph")
                        root.unit_speed.text = getString(R.string.setting_unit_mph)
                    }
                }

                dialog.dismiss()
            }
            alertDialog.setNegativeButton(getString(R.string.dialog_cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            alertDialog.create()
            alertDialog.show()
        }

        root.layout_pressure.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this.requireContext())
            alertDialog.setTitle(getString(R.string.setting_pressureUnits))

            val tempArray = resources.getStringArray(R.array.pressureUnits)

            alertDialog.setItems(
                tempArray
            ) { dialog, index ->
                when (index) {
                    0 -> {
                        sharedRepository.setPressureUnit("hPa/mBar")
                        root.unit_pressure.text = getString(R.string.setting_unit_hpa)
                    }
                    1 -> {
                        sharedRepository.setPressureUnit("kPa")
                        root.unit_pressure.text = getString(R.string.setting_unit_kpa)
                    }
                    2 -> {
                        sharedRepository.setPressureUnit("mm Hg")
                        root.unit_pressure.text = getString(R.string.setting_unit_mmhg)
                    }
                    3 -> {
                        sharedRepository.setPressureUnit("in Hg")
                        root.unit_pressure.text = getString(R.string.setting_unit_inhg)
                    }
                }

                dialog.dismiss()
            }
            alertDialog.setNegativeButton(getString(R.string.dialog_cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            alertDialog.create()
            alertDialog.show()
        }

        root.layout_refresh.setOnClickListener {
            val alertDialog = AlertDialog.Builder(this.requireContext())
            alertDialog.setTitle(getString(R.string.setting_refreshInterval))

            val tempArray = resources.getStringArray(R.array.refreshIntervals)

            alertDialog.setItems(
                tempArray
            ) { dialog, index ->
                when (index) {
                    0 -> {
                        sharedRepository.setRefreshInt("0")
                        root.unit_refresh.text = getString(R.string.settings_none)
                    }
                    1 -> {
                        sharedRepository.setRefreshInt("15")
                        root.unit_refresh.text = getString(R.string.settings_15min)
                    }
                    2 -> {
                        sharedRepository.setRefreshInt("30")
                        root.unit_refresh.text = getString(R.string.settings_30min)
                    }
                    3 -> {
                        sharedRepository.setRefreshInt("1")
                        root.unit_refresh.text = getString(R.string.settings_1hour)
                    }
                    4 -> {
                        sharedRepository.setRefreshInt("2")
                        root.unit_refresh.text = getString(R.string.settings_2hour)
                    }
                    5 -> {
                        sharedRepository.setRefreshInt("6")
                        root.unit_refresh.text = getString(R.string.settings_6hour)
                    }
                    6 -> {
                        sharedRepository.setRefreshInt("12")
                        root.unit_refresh.text = getString(R.string.settings_12hour)
                    }
                    7 -> {
                        sharedRepository.setRefreshInt("24")
                        root.unit_refresh.text = getString(R.string.settings_24hour)
                    }
                }

                dialog.dismiss()
            }
            alertDialog.setNegativeButton(getString(R.string.dialog_cancel)) { dialog, _ ->
                dialog.dismiss()
            }
            alertDialog.create()
            alertDialog.show()
        }
    }

    override fun onDestroy() {
        super.onDestroy()
        close()
    }

    fun close() {
        val intent = Intent(activity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        val bundle = Bundle()
        bundle.putBoolean(MainActivity.SHOULD_REFRESH_FLAG, true)
        intent.putExtras(bundle)
        startActivity(intent)
    }
}