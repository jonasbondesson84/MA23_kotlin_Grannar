package com.example.grannar

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [EventFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class EventFragment : Fragment(), EventAdapter.MyAdapterListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var rvEvents: RecyclerView
    private var eventList = mutableListOf<Event>()
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: EventAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_event, container, false)
        db = Firebase.firestore


        rvEvents = view.findViewById(R.id.rvEventsList)
        rvEvents.layoutManager = LinearLayoutManager(view.context)
        rvEvents.addItemDecoration(
            DividerItemDecoration(requireContext(), LinearLayoutManager.VERTICAL
            )
        )
        adapter = EventAdapter(view.context, eventList, this)
        rvEvents.adapter = adapter
        getEvents()

        val fabAddEvent: FloatingActionButton = view.findViewById(R.id.fabAddEvent)




        fabAddEvent.setOnClickListener {
            val dialogFragment = AddEventDialogFragment()

            dialogFragment.show(parentFragmentManager, "AddEventDialogFragment")
        }




        return view
    }

    private fun getEvents() {

        db.collection("Events").addSnapshotListener { snapshot, error ->
            if (snapshot != null) {
                eventList.clear()
                for (document in snapshot.documents) {
                    val event = document?.toObject<Event>()
                    if (event != null) {
                        //set lastRead position
                        eventList.add(event)
                    }
                }
                eventList.sortBy { it.startDateTime }
                adapter.notifyDataSetChanged()
            }
        }
    }

    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment EventFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            EventFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun goToEvent(event: Event) {
        val eventID = event.docID
        Log.d("!!!", eventID.toString())
        if(eventID != null) {
            val action =
                EventFragmentDirections.actionEventFragmentToEventInfoFragment(event.docID!!)
            findNavController().navigate(action)
        }
    }
}