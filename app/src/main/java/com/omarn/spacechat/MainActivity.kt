package com.omarn.spacechat

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import android.view.Menu
import android.view.MenuItem
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.viewpager.widget.ViewPager
import com.google.android.material.tabs.TabLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.database.*
import com.omarn.spacechat.Fragments.ChatsFragment
import com.omarn.spacechat.Fragments.SearchFragment
import com.omarn.spacechat.Fragments.SettingsFragment
import com.omarn.spacechat.ModelClasses.Chat
import com.omarn.spacechat.ModelClasses.ChatList
import com.omarn.spacechat.ModelClasses.Users
import com.squareup.picasso.Picasso

import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    var refUSers: DatabaseReference? = null
    var firebaseUser: FirebaseUser? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        setSupportActionBar(toolbar_main)

        //val toolbar: Toolbar = findViewById(R.id.toolbar_main)
        //setSupportActionBar(toolbar)
        supportActionBar!!.title = ""

        firebaseUser = FirebaseAuth.getInstance().currentUser
        refUSers = FirebaseDatabase.getInstance().reference.child("Users").child(firebaseUser!!.uid)

        val tabLayout: TabLayout = findViewById(R.id.tablayout)
        val viewPager: ViewPager = findViewById(R.id.view_pager)


        val ref = FirebaseDatabase.getInstance().reference.child("Chats")
        ref!!.addValueEventListener(object : ValueEventListener{
            override fun onDataChange(p0: DataSnapshot) {
                val viewPagerAdapter = viewPagerAdapter(supportFragmentManager)

                var countUnreadM = 0

                for(dataSnapShot in p0.children) {

                    val chat = dataSnapShot.getValue(Chat::class.java)

                    if(chat!!.getReceiver().equals(firebaseUser!!.uid) && !chat.getIsseen()!!) {
                        countUnreadM = countUnreadM + 1
                    }

                }

                if(countUnreadM == 0) {
                    viewPagerAdapter.addFragment(ChatsFragment(),"chats")
                } else {
                    viewPagerAdapter.addFragment(ChatsFragment(),"($countUnreadM)chats")
                }

                viewPagerAdapter.addFragment(SearchFragment(),"Search")
                viewPagerAdapter.addFragment(SettingsFragment(),"Settings")

                viewPager.adapter = viewPagerAdapter
                tabLayout.setupWithViewPager(viewPager)


            }

            override fun onCancelled(p0: DatabaseError) {

            }
        })


        refUSers!!.addValueEventListener(object : ValueEventListener{

            override fun onDataChange(p0: DataSnapshot) {
            if(p0.exists()) {
                val user : Users? = p0.getValue(Users::class.java)

                Username.text = user!!.getUsername()
                Picasso.get().load(user.getProfile()).placeholder(R.drawable.profileplaceholder).into(profile_image)
            }
            }

            override fun onCancelled(p0: DatabaseError) {

            }

        })

    }


    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
         when (item.itemId) {
            R.id.action_logout -> {
                FirebaseAuth.getInstance().signOut()
                val intent = Intent(this@MainActivity, WelcomeActivity::class.java)
                startActivity(intent)
                finish()
                return true
            }

        }
        return false
    }

    internal class viewPagerAdapter(fragmentManager: FragmentManager) :
        FragmentPagerAdapter(fragmentManager) {

        private val fragments: ArrayList<Fragment>
        private val titles: ArrayList<String>

        init {
            fragments = ArrayList()
            titles = ArrayList()
        }

        override fun getItem(position: Int): Fragment {
            return fragments[position]
        }

        override fun getCount(): Int {
            return fragments.size
        }

        fun addFragment(fragment: Fragment, title: String) {
            fragments.add(fragment)
            titles.add(title)
        }

        override fun getPageTitle(position: Int): CharSequence? {
            return titles[position]
        }


    }
}
