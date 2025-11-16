package com.labs.fleamarketapp.adapter

import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.labs.fleamarketapp.fragments.ProfileSectionFragment

class ProfilePagerAdapter(
    fragment: Fragment,
    private val sections: List<ProfileSection>
) : FragmentStateAdapter(fragment) {

    override fun getItemCount(): Int = sections.size

    override fun createFragment(position: Int): Fragment {
        val section = sections[position]
        return ProfileSectionFragment.newInstance(section.title, section.description)
    }
}

data class ProfileSection(
    val title: String,
    val description: String
)

