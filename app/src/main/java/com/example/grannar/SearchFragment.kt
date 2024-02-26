package com.example.grannar

import android.app.Dialog
import android.content.Context
import android.content.res.ColorStateList
import android.graphics.Color
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import android.widget.Button
import android.widget.CheckBox
import android.widget.EditText
import android.widget.Filter
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.material.chip.Chip
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.slider.Slider
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject
import kotlin.math.cos

// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchFragment : Fragment(), SearchListAdapter.MyAdapterListener,  SignInResultListener, DistanceSliderListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var searchList = mutableListOf<User>()
    private var listInRecyclerView = mutableListOf<User>()
    private var listAfterCategoryFilter = mutableListOf<User>()
    private lateinit var db : FirebaseFirestore
    private lateinit var adapter: SearchListAdapter
    private lateinit var etvSearch: EditText
    private lateinit var fabFilter: FloatingActionButton
    private lateinit var tvNoSearchResult: TextView
    private var selectedCategories = mutableListOf<String>()
    private lateinit var distanceChip: Chip
    private var distanceSet: Float = 5f

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        db = Firebase.firestore
        adapter = SearchListAdapter(requireContext(), listInRecyclerView, this)
        getUsersList()

    }



    fun getUsersList() {
        val auth = FirebaseAuth.getInstance()
        val userIdToRemove = auth.currentUser?.uid

        Log.d("!!!","Current-User: ${CurrentUser.userID}")
        searchList.clear()
        Log.d("!!!", db.toString())

        val query = if (userIdToRemove != null){
            db.collection("users").whereNotEqualTo("userID", userIdToRemove)
        }else{
            db.collection("users")
        }

        query.get().addOnSuccessListener { result ->
            for ((i, document) in result.withIndex()) {
                Log.d("!!!", "${document.id} => ${document.data}")
                val user = document.toObject<User>()
                Log.d("!!!", "UserID: ${CurrentUser.userID}")
                searchList.add(user)


            }

            setListInRecyclerView(true)
        }
            .addOnFailureListener { exception ->
                Log.d("!!!", "Error getting documents: ", exception)
            }

    }

//    private fun getUsersWithinDistance(distanceInKilometers: Int){
//        val lat = CurrentUser.locLat
//        val lng = CurrentUser.locLng
//        val loggedInUserID = FirebaseAuth.getInstance().currentUser?.uid
//
//        if (lat != null && lng != null){
//            val center = GeoLocation(lat, lng)
//            val radiusInM = distanceInKilometers * 1000
//
//            // Get all surrounding geo hashes within radius
//            val bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM.toDouble())
//            val tasks: MutableList<Task<QuerySnapshot>> = ArrayList()
//            // Query for all users with the same hash as in the list
//            for (b in bounds){
//                val query = db.collection("users")
//                    .orderBy("geoHash")
//                    .startAt(b.startHash)
//                    .endAt(b.endHash)
//                tasks.add(query.get())
//            }
//            // Loop through all results from all querys
//            Tasks.whenAllComplete(tasks)
//                .addOnSuccessListener {
//                    val matchingDocuments: MutableList<DocumentSnapshot> = ArrayList()
//                    for (task in tasks){
//                        val snapshot = task.result
//                        for (doc in snapshot.documents){
//                            val docLat = doc.getDouble("locLat")
//                            val docLng = doc.getDouble("locLng")
//                            if (docLat != null && docLng != null){
//                                val docLoc = GeoLocation(docLat, docLng)
//                                Log.d("!!!", "${doc.getDouble("locLat")}")
//                                Log.d("!!!", "${doc.getString("firstName")}")
//                                Log.d("!!!", "Distance M: ${GeoFireUtils.getDistanceBetween(docLoc, center)}")
//                                // Remove the false positive, that has the same hash but is still outside of the radius
//                                val distanceToUserInM = GeoFireUtils.getDistanceBetween(docLoc, center)
//                                if (distanceToUserInM <= radiusInM){
//                                    matchingDocuments.add(doc)
//                                    Log.d("!!!", "Matching documents: ${matchingDocuments.size}")
//                                }else{
//                                    Log.d("!!!", "False Positive Document!!!")
//                                }
//                            }
//                        }
//                    }
//                    searchList.clear()
//                    for (document in matchingDocuments){
//
//                        val user = document.toObject<User>()
//                        if (user != null && user.userID != loggedInUserID) {
//                            searchList.add(user)
//                            Log.d("!!!", "User with Name added: ${user.firstName}")
//                        }
//                    }
//                    Log.d("!!!", "SearchList: ${searchList.size}")
//                    setListInRecyclerView(true)
//                }
//        }
//    }

    private fun getUsersWithinDistance(distanceInKilometers: Int){
        val lat = CurrentUser.locLat
        val lng = CurrentUser.locLng


        if (lat != null && lng != null) {
            val center = GeoLocation(lat, lng)
            val radiusInM = distanceInKilometers * 1000
            val tasks = getListOfDbQueries(radiusInM, center)
            queryForUsersByGeoHash(tasks, center, radiusInM)

        }
    }

    private fun getListOfDbQueries(radiusInM: Int, center: GeoLocation):  MutableList<Task<QuerySnapshot>>{

            // Get all surrounding geo hashes within radius
            val bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM.toDouble())
            val tasks: MutableList<Task<QuerySnapshot>> = ArrayList()
            // Query for all users with the same hash as in the list
            for (b in bounds) {
                val query = db.collection("users")
                    .orderBy("geoHash")
                    .startAt(b.startHash)
                    .endAt(b.endHash)
                tasks.add(query.get())
            }
            return tasks
    }

    private fun queryForUsersByGeoHash(tasks: MutableList<Task<QuerySnapshot>>, center: GeoLocation, radiusInM: Int){
        Tasks.whenAllComplete(tasks)
            .addOnSuccessListener {
                val matchingDocuments: MutableList<DocumentSnapshot> = ArrayList()
                for (task in tasks) {
                    val snapshot = task.result
                    for (doc in snapshot.documents) {
                        val docLat = doc.getDouble("locLat")
                        val docLng = doc.getDouble("locLng")
                        if (docLat != null && docLng != null) {
                            val docLoc = GeoLocation(docLat, docLng)
                            Log.d("!!!", "${doc.getDouble("locLat")}")
                            Log.d("!!!", "${doc.getString("firstName")}")
                            Log.d(
                                "!!!",
                                "Distance M: ${GeoFireUtils.getDistanceBetween(docLoc, center)}"
                            )
                            // Remove the false positive, that has the same hash but is still outside of the radius
                            val distanceToUserInM = GeoFireUtils.getDistanceBetween(docLoc, center)
                            if (distanceToUserInM <= radiusInM) {
                                matchingDocuments.add(doc)
                                Log.d("!!!", "Matching documents: ${matchingDocuments.size}")
                            } else {
                                Log.d("!!!", "False Positive Document!!!")
                            }
                        }
                    }
                }
                createUsersAndFilRecycler(matchingDocuments)

            }
    }

    private fun createUsersAndFilRecycler(matchingDocuments: MutableList<DocumentSnapshot>){
        val loggedInUserID = FirebaseAuth.getInstance().currentUser?.uid
        searchList.clear()
        for (document in matchingDocuments){

            val user = document.toObject<User>()
            if (user != null && user.userID != loggedInUserID) {
                searchList.add(user)
                Log.d("!!!", "User with Name added: ${user.firstName}")
            }
        }
        Log.d("!!!", "SearchList: ${searchList.size}")
        setListInRecyclerView(true)

    }




    private fun filterListOnInterestCategory(listToFilter: MutableList<User>):List<User>{
        if (selectedCategories.isEmpty()){
            fabFilter.setImageResource(R.drawable.baseline_filter_alt_off_24)
            fabFilter.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.md_theme_primaryContainer))
            return listToFilter
        }else {
            val filteredList = listToFilter.filter { user ->
                user.interests?.any {interest ->
                    interest.category in selectedCategories
                } == true
            }
            fabFilter.setImageResource(R.drawable.baseline_filter_list_alt_242)
            fabFilter.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E6FF00"))
            return filteredList
        }
    }

    private fun filterListOnInterestName(listToFilter: List<User>): List<User>{

        val interestName = etvSearch.text.toString()
        if (interestName.isEmpty()){
            return listToFilter
        }
        else {
            val filteredList = listToFilter.filter { user ->
                user.interests?.any {interest ->
                    interest?.name?.lowercase()?.contains(interestName.lowercase()) ?: false
                } == true
            }
            return filteredList
        }
    }

    private fun setListInRecyclerView(categoryFilterChanged: Boolean){
        listInRecyclerView.clear()
        if (categoryFilterChanged){
            listAfterCategoryFilter.clear()
            listAfterCategoryFilter = filterListOnInterestCategory(searchList).toMutableList()
        }
        val nameFilteredList = filterListOnInterestName(listAfterCategoryFilter.toList())
        listInRecyclerView.addAll(nameFilteredList)
        tvNoSearchResult.isVisible = listInRecyclerView.isEmpty()
        adapter.notifyDataSetChanged()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_search, container, false)

        val btnGetuser = view.findViewById<Button>(R.id.btnGetUser)
        val rvSearchList = view.findViewById<RecyclerView>(R.id.rvSearchList)
        rvSearchList.layoutManager = LinearLayoutManager(view.context)
        rvSearchList.adapter = adapter

        rvSearchList.addItemDecoration(DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL))
        adapter.onUserClick = {
            val uid = it.userID
            if(uid != null) {
                val action =
                    SearchFragmentDirections.actionSearchFragmentToFriendProfileFragment(uid)
                findNavController().navigate(action)
            }

        }
        btnGetuser.setOnClickListener {
           getUsersWithinDistance(2)

//            val auth = Firebase.auth
//            CurrentUser.clearUser()
//            auth.signOut()
//            adapter.notifyDataSetChanged()
//            Log.d("!!!", CurrentUser.friendsList?.size.toString())
        }
        etvSearch = view.findViewById(R.id.etvSearchInterest)
        addTextChangeListener()
        tvNoSearchResult = view.findViewById(R.id.tvEmptySearchList)


        fabFilter = view.findViewById(R.id.fabFilter)

        if (selectedCategories.isEmpty()){
            fabFilter.setImageResource(R.drawable.baseline_filter_alt_off_24)
            fabFilter.backgroundTintList = ColorStateList.valueOf(resources.getColor(R.color.md_theme_primaryContainer))

        }else{
            fabFilter.setImageResource(R.drawable.baseline_filter_list_alt_242)
            fabFilter.backgroundTintList = ColorStateList.valueOf(Color.parseColor("#E6FF00"))
        }

        fabFilter.setOnClickListener {
            openCategoryFilterDialog()
        }


        distanceChip = view.findViewById<Chip>(R.id.distancseChip)
        distanceChip.text = "Distance: ${distanceSet.toInt()} km"

        distanceChip.setOnClickListener {
            Log.d("!!!", "Chip chip")
            val dialogFragment = DistanceMapDialogFragment(distanceSet)
            dialogFragment.setDistanceSliderListener(this)
            dialogFragment.show(parentFragmentManager, "distanceDialogFragment")
        }



        return view
    }
    private fun addTextChangeListener(){
        etvSearch.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not using
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not using
            }

            override fun afterTextChanged(s: Editable?) {
                setListInRecyclerView(false)
            }

        })
    }


    private fun openDialogFragment() {
        val dialogFragment = SignInDialogFragment()
        dialogFragment.show(parentFragmentManager, "SignInDialogFragment")
    }

    private fun openCategoryFilterDialog(){

        val dialog = Dialog(requireContext())
         dialog.setContentView(R.layout.dialog_category_filter)
        val container = dialog.findViewById<LinearLayout>(R.id.categoryLinearLayout)
        val checkBoxes = mutableListOf<CheckBox>()
        CategoryManager.categories.forEach { (category, colorID) ->

            val checkBox = CheckBox(requireContext())
            checkBox.text = category
            checkBox.setBackgroundColor(ContextCompat.getColor(requireContext(), colorID))

            if (selectedCategories.contains(category)){
                checkBox.isChecked = true
            }
            checkBoxes.add(checkBox)
            container.addView(checkBox)
        }
        container.requestLayout()
        dialog.findViewById<Button>(R.id.btnCancelAddFilter).setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.btnAddCategoryFilter).setOnClickListener {
            setCategoryFilter(checkBoxes, dialog)
        }
        dialog.findViewById<TextView>(R.id.tvClearFilter).setOnClickListener {
            selectedCategories.clear()
            checkBoxes.forEach { checkBox ->
                checkBox.isChecked = false
            }
            setListInRecyclerView(true)
            dialog.dismiss()
        }


        dialog.show()
    }

    private fun setCategoryFilter(checkBoxes: List<CheckBox>, dialog: Dialog){
        selectedCategories.clear()
        checkBoxes.forEach { checkBox ->
            if (checkBox.isChecked)
                selectedCategories.add(checkBox.text.toString())
        }
        Log.d("!!!", selectedCategories.toString())
        setListInRecyclerView(true)

        dialog.dismiss()

    }





    companion object {
        /**
         * Use this factory method to create a new instance of
         * this fragment using the provided parameters.
         *
         * @param param1 Parameter 1.
         * @param param2 Parameter 2.
         * @return A new instance of fragment SeachFragment.
         */
        // TODO: Rename and change types and number of parameters
        @JvmStatic
        fun newInstance(param1: String, param2: String) =
            SearchFragment().apply {
                arguments = Bundle().apply {
                    putString(ARG_PARAM1, param1)
                    putString(ARG_PARAM2, param2)
                }
            }
    }

    override fun onAddFriendsListener(user: User) {
        openDialogFragment()
    }

    override fun onSendMessageListener(user: User) {
        val uid = user.userID
        if(uid != null) {
            val action =
                SearchFragmentDirections.actionSearchFragmentToChatFragment(uid)
            findNavController().navigate(action)
        }
    }

    override fun onSignInSuccess() { //MÅSTE FIXA SÅ DET UPPDATERAS NÄR MAN LOGGAR IN
        //adapter.notifyDataSetChanged()
        getUsersList()
        Log.d("!!!", "onSigntInSuccess @ SearchFragment")
    }

    override fun onSignInFailure() {

    }

    override fun onSignUpPress() {

        Log.d("!!!", "SignUpPress")

    }

    override fun onDistanceSet(distance: Double) {
        distanceSet = distance.toFloat()
        getUsersWithinDistance(distance.toInt())
        distanceChip.text = "Distance: $distanceSet km"

    }
}