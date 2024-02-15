package com.example.grannar

import android.graphics.Canvas
import android.graphics.Color
import android.os.Bundle
import android.util.DisplayMetrics
import android.util.Log
import android.util.TypedValue
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import kotlin.math.abs
import kotlin.math.roundToInt

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [FriendsListFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class FriendsListFragment : Fragment(), SearchListAdapter.MyAdapterListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private lateinit var adapter: SearchListAdapter
    private val Int.dp
        get() = TypedValue.applyDimension(
            TypedValue.COMPLEX_UNIT_DIP,
            toFloat(), resources.displayMetrics
        ).roundToInt()

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
        val view = inflater.inflate(R.layout.fragment_friends_list, container, false)
        val displayMetrics: DisplayMetrics = resources.displayMetrics
        val width = (displayMetrics.widthPixels / displayMetrics.density).toInt().dp
        var deleteIcon = resources.getDrawable(R.drawable.baseline_delete_24, null)

        val rvSearchList = view.findViewById<RecyclerView>(R.id.rvSearchListFriends)
        rvSearchList.layoutManager = LinearLayoutManager(view.context)


        adapter = CurrentUser.friendsList?.let { SearchListAdapter(view.context, it, this) } ?: SearchListAdapter(view.context, mutableListOf(), this)
        rvSearchList.adapter = adapter
        adapter.onUserClick = {
            val uid = it.userID
            if (uid != null) {
                val action =
                    FriendsListFragmentDirections.actionFriendsListFragmentToFriendProfileFragment(uid)
                    //SearchFragmentDirections.actionSearchFragmentToFriendProfileFragment(uid)
                findNavController().navigate(action)
            }
        }

        val itemTouchHelper = ItemTouchHelper(object : ItemTouchHelper.SimpleCallback(0, ItemTouchHelper.LEFT){
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                TODO("Not yet implemented")
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                Log.d("!!!", "swiped")
                showDeleteDialog(view, viewHolder.adapterPosition)

            }

            override fun onChildDraw(
                c: Canvas,
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                dX: Float,
                dY: Float,
                actionState: Int,
                isCurrentlyActive: Boolean
            ) {
                when {
                    abs(dX) < width / 3 -> c.drawColor(Color.GRAY)

                    else -> c.drawColor(Color.RED)
                }
                val textMargin = 16.dp
                val desiredIconSize = 40.dp
                val iconWidth = desiredIconSize
                val iconHeight = desiredIconSize

                val iconLeft = width - textMargin - iconWidth
                val iconRight = width - textMargin
                val iconTop = viewHolder.itemView.top + textMargin + 8.dp
                val iconBottom = iconTop + iconHeight

                deleteIcon.setBounds(iconLeft, iconTop, iconRight, iconBottom)

                deleteIcon.draw(c)

                super.onChildDraw(
                    c,
                    recyclerView,
                    viewHolder,
                    dX,
                    dY,
                    actionState,
                    isCurrentlyActive
                )
            }

        })
        itemTouchHelper.attachToRecyclerView(rvSearchList)

//        adapter.onUserClick = {
//            findNavController().navigate(R.id.)
//        }


        return view
    }
    private fun showDeleteDialog(view: View, adapterPosition: Int) {
        context?.let {
            MaterialAlertDialogBuilder(it)
                .setTitle("Warning")
                .setMessage("Do you want to remove ${CurrentUser.friendsList?.get(adapterPosition)?.firstName} from your friendlist?")
                .setNegativeButton("No") { dialog, which ->
                    // Respond to negative button press
                    adapter.notifyDataSetChanged()
                }
                .setPositiveButton("Yes") { dialog, which ->
                    // Respond to positive button press
                    adapter.removeFriend(adapterPosition)

                }
                .show()
        }
    }


    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment FriendsListFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            FriendsListFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onAddFriendsListener(user: User) {

    }
}