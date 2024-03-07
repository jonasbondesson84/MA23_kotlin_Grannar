package com.example.grannar

import android.app.Dialog
import android.content.res.ColorStateList
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.WindowManager
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.core.view.doOnPreDraw
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.firebase.geofire.GeoFireUtils
import com.firebase.geofire.GeoLocation
import com.google.android.gms.tasks.Task
import com.google.android.gms.tasks.Tasks
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.tabs.TabLayout
import com.google.android.material.textfield.TextInputLayout
import com.google.android.material.transition.MaterialElevationScale
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.QuerySnapshot
import com.google.firebase.firestore.firestore
import com.google.firebase.firestore.toObject


// TODO: Rename parameter arguments, choose names that match
// the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
private const val ARG_PARAM1 = "param1"
private const val ARG_PARAM2 = "param2"

/**
 * A simple [Fragment] subclass.
 * Use the [SearchFragment.newInstance] factory method to
 * create an instance of this fragment.
 */
class SearchFragment : Fragment(), SearchListAdapter.MyAdapterListener, SignInResultListener,
    DistanceSliderListener {
    // TODO: Rename and change types of parameters
    private var param1: String? = null
    private var param2: String? = null
    private var searchList = mutableListOf<User>() // The whole downloaded list to filter
    private var listInRecyclerView = mutableListOf<User>() // The list that is in the recyclerView
    private var listAfterCategoryFilter =
        mutableListOf<User>() // The downloaded list but filter from dialog
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: SearchListAdapter
    private lateinit var etvSearch: EditText
    private lateinit var filterChip: Chip
    private lateinit var tilSearch: TextInputLayout
    private lateinit var tvNoSearchResult: TextView
    private var selectedCategories = mutableListOf<String>()
    private var selectedGenders = mutableListOf<String>()
    private var genders = listOf("Male", "Female", "Non-Binary")
    private lateinit var distanceChip: Chip
    private var distanceSet: Float = 5f
    private lateinit var tabFriends: TabLayout
    private var onMyFriends = false
    private var friendsList = mutableListOf<User>()

    private var filterString = ""
    private var searchString = ""


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            param1 = it.getString(ARG_PARAM1)
            param2 = it.getString(ARG_PARAM2)
        }

        db = Firebase.firestore
        adapter = SearchListAdapter(requireContext(), listInRecyclerView, this)
        getUsersWithinDistance(5)


    }

    fun getUsersWithinDistance(distanceInKilometers: Int) {
        val lat = CurrentUser.locLat
        val lng = CurrentUser.locLng
        val center = GeoLocation(lat ?: 59.334591, lng ?: 18.063240)
        val radiusInM = distanceInKilometers * 1000
        val tasks = getListOfDbQueries(radiusInM, center)
        queryForUsersByGeoHash(tasks, center, radiusInM)
    }

    private fun getListOfDbQueries(
        radiusInM: Int,
        center: GeoLocation
    ): MutableList<Task<QuerySnapshot>> {

        // Get all surrounding geo hashes within radius
        val bounds = GeoFireUtils.getGeoHashQueryBounds(center, radiusInM.toDouble())
        val tasks: MutableList<Task<QuerySnapshot>> = ArrayList()
        // Query for all users with the same hash as in the list
        for (b in bounds) {
            val query = db.collection("users")
                .orderBy("geoHash")
                .startAt(b.startHash)
                .endAt(b.endHash)
                .limit(30)
            tasks.add(query.get())
        }
        return tasks
    }

    private fun queryForUsersByGeoHash(
        tasks: MutableList<Task<QuerySnapshot>>,
        center: GeoLocation,
        radiusInM: Int
    ) {
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
                            // Remove the false positive, that has the same hash but is still outside of the radius
                            val distanceToUserInM = GeoFireUtils.getDistanceBetween(docLoc, center)
                            if (distanceToUserInM <= radiusInM) {
                                matchingDocuments.add(doc)

                            } else {

                            }
                        }
                    }
                }
                createUsersAndFilRecycler(matchingDocuments)

            }
    }

    private fun createUsersAndFilRecycler(matchingDocuments: MutableList<DocumentSnapshot>) {
        val loggedInUserID = FirebaseAuth.getInstance().currentUser?.uid
        searchList.clear()
        for (document in matchingDocuments) {

            val user = document.toObject<User>()
            if (user != null && user.userID != loggedInUserID) {
                searchList.add(user)

            }
        }

        setListInRecyclerView(true)

    }


    private fun filterListOnInterestCategory(listToFilter: MutableList<User>): List<User> {

        val filteredList = if (selectedCategories.isEmpty() && selectedGenders.isEmpty()) {
            listToFilter.toList() // Returns a copy of the original list if no filers is selected
        } else {
            var tempList = listToFilter.toMutableList()

            if (selectedCategories.isNotEmpty()) {
                tempList = tempList.filter { user ->
                    user.interests?.any { interest ->
                        interest.category in selectedCategories
                    } == true
                }.toMutableList()
            }

            if (selectedGenders.isNotEmpty()) {
                tempList = tempList.filter { user ->
                    user.gender in selectedGenders
                }.toMutableList()
            }


            tempList.toList() // Returnera den filtrerade listan
        }

        return filteredList
    }

    private fun filterListOnInterestName(listToFilter: List<User>): List<User> {

        val interestName = etvSearch.text.toString()
        if (interestName.isEmpty()) {
            return listToFilter
        } else {
            val filteredList = listToFilter.filter { user ->
                user.interests?.any { interest ->
                    interest?.name?.lowercase()?.contains(interestName.lowercase()) ?: false
                } == true
            }
            return filteredList
        }
    }

    private fun setListInRecyclerView(categoryFilterChanged: Boolean) {

        listInRecyclerView.clear()
        if (categoryFilterChanged) {
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
        postponeEnterTransition()
        view.doOnPreDraw { startPostponedEnterTransition() }
        exitTransition = MaterialElevationScale(/* growing= */ false)
        reenterTransition = MaterialElevationScale(/* growing= */ true)


        val rvSearchList = view.findViewById<RecyclerView>(R.id.rvSearchList)
        tabFriends = view.findViewById(R.id.tabFriends)
        tilSearch = view.findViewById(R.id.tilSearchInterest)
        rvSearchList.layoutManager = LinearLayoutManager(view.context)
        rvSearchList.adapter = adapter
        rvSearchList.addItemDecoration(
            DividerItemDecoration(
                requireContext(),
                DividerItemDecoration.VERTICAL
            )
        )
        tabFriends.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                if (tab != null) {
                    when (tab.position) {
                        0 -> {
                            distanceChip.visibility = View.VISIBLE
                            filterChip.visibility = View.VISIBLE
                            onMyFriends = false
                            etvSearch.setText(searchString)
                            tilSearch.hint = "Search Interest"
                            setListInRecyclerView(false)
                            adapter = SearchListAdapter(
                                requireContext(),
                                listInRecyclerView,
                                this@SearchFragment
                            )
                            rvSearchList.adapter = adapter
                            tvNoSearchResult.visibility = View.INVISIBLE
                            CurrentUser.tabFriendItem = 0
                        }

                        1 -> {
                            distanceChip.visibility = View.GONE
                            filterChip.visibility = View.GONE
                            onMyFriends = true
                            filterList(filterString)
                            adapter =
                                SearchListAdapter(view.context, friendsList, this@SearchFragment)
                            rvSearchList.adapter = adapter
                            etvSearch.setText(filterString)
                            tilSearch.hint = "Search Friend"
                            tvNoSearchResult.visibility = View.INVISIBLE
                            CurrentUser.tabFriendItem = 1
                        }

                        else -> {

                        }
                    }
                }
            }

            override fun onTabUnselected(tab: TabLayout.Tab?) {

            }

            override fun onTabReselected(tab: TabLayout.Tab?) {

            }


        })



        etvSearch = view.findViewById(R.id.etvSearchInterest)
        addTextChangeListener()
        tvNoSearchResult = view.findViewById(R.id.tvEmptySearchList)

        filterChip = view.findViewById(R.id.filterChip)
        filterChip.setOnClickListener {
            openFilterDialog()
        }

        distanceChip = view.findViewById(R.id.distanceChip)
        distanceChip.text = "Distance: ${distanceSet.toInt()} km"

        distanceChip.setOnClickListener {

            val dialogFragment = DistanceMapDialogFragment(distanceSet)
            dialogFragment.setDistanceSliderListener(this)
            dialogFragment.show(parentFragmentManager, "distanceDialogFragment")
        }



        return view
    }

    override fun onResume() {
        super.onResume()
        tabFriends.getTabAt(CurrentUser.tabFriendItem)?.select()
    }

    private fun addTextChangeListener() {
        etvSearch.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {
                // Not using
            }

            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                // Not using
            }

            override fun afterTextChanged(s: Editable?) {
                val textInSearchField = s.toString().lowercase().trim()
                if (onMyFriends) {
                    filterString = textInSearchField
                    filterList(textInSearchField)
                } else {
                    searchString = textInSearchField
                    setListInRecyclerView(false)
                }

            }
        })
    }


    private fun openDialogFragment() {
        val dialogFragment = SignInDialogFragment()
        dialogFragment.show(parentFragmentManager, "SignInDialogFragment")
    }

    private fun openFilterDialog() {

        val dialog = Dialog(requireContext())
        dialog.setContentView(R.layout.dialog_category_filter)
        dialog.setOnDismissListener {
            setFilterChipState()
        }

        val categoryChipGroup = dialog.findViewById<ChipGroup>(R.id.categoryChipGroup)
        val categoryChips = createCategoryChips(categoryChipGroup)

        val genderChipGroup = dialog.findViewById<ChipGroup>(R.id.genderChipGroup)
        val genderChips = createGenderChips(genderChipGroup)


        dialog.findViewById<Button>(R.id.btnCancelAddFilter).setOnClickListener {
            dialog.dismiss()
        }

        dialog.findViewById<Button>(R.id.btnAddCategoryFilter).setOnClickListener {
            setCategoryFilter(categoryChips, genderChips, dialog)
        }
        dialog.findViewById<TextView>(R.id.tvClearFilter).setOnClickListener {
            clearFilterChips(categoryChips, genderChips)
            setListInRecyclerView(true)
            dialog.dismiss()
        }

        val window = dialog.window
        window?.setLayout(
            WindowManager.LayoutParams.MATCH_PARENT,
            WindowManager.LayoutParams.WRAP_CONTENT
        )
        dialog.show()
    }

    private fun clearFilterChips(categoryChips: MutableList<Chip>, genderChips: List<Chip>) {
        selectedCategories.clear()
        categoryChips.forEach { checkBox ->
            checkBox.isChecked = false
        }
        selectedGenders.clear()
        genderChips.forEach { chip ->
            chip.isChecked = false
        }
    }


    private fun createCategoryChips(chipGroup: ChipGroup): MutableList<Chip> {
        val categoryChips = mutableListOf<Chip>()
        val inflater = LayoutInflater.from(requireContext())
        CategoryManager.categories.forEach { (category, colorID) ->
            val chip = inflater.inflate(R.layout.chip_layout, chipGroup, false) as Chip
            chip.text = category
            val backgroundColor = ContextCompat.getColor(requireContext(), colorID)
            chip.chipBackgroundColor = ColorStateList.valueOf(backgroundColor)

            val textColor = ContextCompat.getColor(
                requireContext(),
                CategoryManager.getCategoryTextColorID(category)
            )
            chip.setTextColor(textColor)
            chip.isChecked = selectedCategories.contains(category)
            categoryChips.add(chip)
            chipGroup.addView(chip)
        }
        return categoryChips
    }

    private fun createGenderChips(chipGroup: ChipGroup): MutableList<Chip> {
        val genderChips = mutableListOf<Chip>()
        val inflater = LayoutInflater.from(requireContext())
        genders.forEach { gender ->

            val chip = inflater.inflate(R.layout.chip_layout, chipGroup, false) as Chip
            chip.text = gender
            chip.isChecked = selectedGenders.contains(gender)
            genderChips.add(chip)
            chipGroup.addView(chip)
        }
        return genderChips
    }

    private fun setCategoryFilter(
        categoryChips: List<Chip>,
        genderChips: List<Chip>,
        dialog: Dialog
    ) {
        selectedCategories.clear()
        categoryChips.forEach { chip ->
            if (chip.isChecked)
                selectedCategories.add(chip.text.toString())
        }

        selectedGenders.clear()
        genderChips.forEach { chip ->
            if (chip.isChecked) {
                selectedGenders.add(chip.text.toString())
            }
        }
        setListInRecyclerView(true)

        dialog.dismiss()

    }

    private fun setFilterChipState() {
        filterChip.isChecked = selectedGenders.isNotEmpty() || selectedCategories.isNotEmpty()
        val numberOfFilters =
            if (selectedGenders.size + selectedCategories.size == 0) {
                ""
            } else {
                (selectedGenders.size + selectedCategories.size).toString()
            }
        filterChip.text = "${resources.getString(R.string.filter)} $numberOfFilters "
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
        if (uid != null) {
            val action =
                SearchFragmentDirections.actionSearchFragmentToChatFragment(uid)
            findNavController().navigate(action)
        }
    }

    override fun goToUser(user: User, card: ConstraintLayout) {
        val uid = user.userID
        if (uid != null) {
            val extra = FragmentNavigatorExtras(card to uid)
            val action =
                SearchFragmentDirections.actionSearchFragmentToFriendProfileFragment(uid)
            findNavController().navigate(action, extra)
        }
    }

    override fun onSignInSuccess() {
        getUsersWithinDistance(distanceSet.toInt())
    }

    override fun onSignInFailure() {

    }

    override fun onSignUpPress() {

    }

    override fun onDistanceSet(distance: Double) {
        distanceSet = distance.toFloat()
        getUsersWithinDistance(distance.toInt())
        distanceChip.text = "Distance: ${distanceSet.toInt()} km"

    }

    private fun addFriendsListToRecycler(listToAdd: List<User>) {
        friendsList.clear()
        friendsList.addAll(listToAdd)
        adapter.notifyDataSetChanged()
        tvNoSearchResult.isVisible = listToAdd.isEmpty()

    }

    private fun filterList(textToSearchFor: String?) {
        if (textToSearchFor != null) {
            val filteredList = CurrentUser.friendsList?.filter { user ->
                user.firstName?.lowercase()?.contains(textToSearchFor) ?: false
            }

            if (filteredList != null) {
                addFriendsListToRecycler(filteredList)
            }
        } else {
            CurrentUser.friendsList?.let { addFriendsListToRecycler(it) }
        }
    }

}