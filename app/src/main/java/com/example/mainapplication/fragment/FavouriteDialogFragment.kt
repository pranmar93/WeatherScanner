package com.example.mainapplication.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.mainapplication.R
import com.example.mainapplication.activities.MainActivity
import com.example.mainapplication.activities.sharedRepository
import com.example.mainapplication.adapter.FavouriteRecyclerAdapter
import com.example.mainapplication.models.Weather
import kotlinx.android.synthetic.main.fragment_favourite_location.view.*
import org.json.JSONException

class FavouriteDialogFragment: Fragment(), FavouriteRecyclerAdapter.ItemClickListener {

    private var recyclerAdapter: FavouriteRecyclerAdapter? = null
    private lateinit var root: View

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        root = inflater.inflate(R.layout.fragment_favourite_location, container, false)
        return root
    }

    override fun onViewCreated(
        view: View,
        savedInstanceState: Bundle?
    ) {
        super.onViewCreated(view, savedInstanceState)
        val toolbar: Toolbar = view.findViewById(R.id.dialogToolbar)
        val recyclerView: RecyclerView = view.findViewById(R.id.favouriteRecyclerView)
        toolbar.title = getString(R.string.location_favourite_heading)
        toolbar.setNavigationIcon(R.drawable.ic_close_black_24dp)
        toolbar.setNavigationOnClickListener {
            val intent = Intent(activity, MainActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
            val bundle = Bundle()
            bundle.putBoolean(MainActivity.SHOULD_REFRESH_FLAG, true)
            intent.putExtras(bundle)
            startActivity(intent)
            close()
        }

        try {
            val favouriteCities = sharedRepository.getFavourites()

            if (favouriteCities.isEmpty()) {
                root.favouriteRecyclerView.visibility = View.GONE
                root.no_favView.visibility = View.VISIBLE
            } else {
                root.favouriteRecyclerView.visibility = View.VISIBLE
                root.no_favView.visibility = View.GONE
            }

            recyclerAdapter = FavouriteRecyclerAdapter(
                this,
                favouriteCities
            )
            recyclerAdapter!!.setClickListener(this@FavouriteDialogFragment)
            recyclerView.layoutManager = LinearLayoutManager(activity)
            recyclerView.adapter = recyclerAdapter
        } catch (e: JSONException) {
            e.printStackTrace()
        }
    }

    fun updateView() {
        root.favouriteRecyclerView.visibility = View.GONE
        root.no_favView.visibility = View.VISIBLE
    }

    override fun onItemClickListener(view: View?, position: Int) {
        val weather: Weather = recyclerAdapter!!.getItem(position)
        val intent = Intent(activity, MainActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_SINGLE_TOP or Intent.FLAG_ACTIVITY_CLEAR_TOP
        val bundle = Bundle()
        sharedRepository.setCityID(weather.cityId!!)
        bundle.putBoolean(MainActivity.SHOULD_REFRESH_FLAG, true)
        intent.putExtras(bundle)
        startActivity(intent)
        close()
    }

    private fun close() {
        val activity = activity
        activity?.supportFragmentManager?.popBackStack()
    }
}