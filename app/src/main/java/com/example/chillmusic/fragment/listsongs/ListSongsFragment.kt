package com.example.chillmusic.fragment.listsongs

import android.content.*
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.widget.PopupMenu
import androidx.appcompat.widget.SearchView
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.widget.TextViewCompat
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.Fragment
import com.example.chillmusic.R
import com.example.chillmusic.`object`.ListSongManager
import com.example.chillmusic.`object`.PreferencesManager
import com.example.chillmusic.activity.MainActivity
import com.example.chillmusic.activity.MusicPlayerActivity
import com.example.chillmusic.adapter.ListSongsAdapter
import com.example.chillmusic.databinding.FragmentListSongsBinding
import com.example.chillmusic.fragment.BottomMenuFragment
import com.example.chillmusic.model.Song
import com.example.chillmusic.service.MusicPlayerService

class ListSongsFragment : Fragment() {
    var _binding: FragmentListSongsBinding? = null
    val binding get() = _binding!!
    lateinit var adapter : ListSongsAdapter
    private val mainActivity get() = activity as MainActivity

    companion object{
        private const val ARG_SONG = "SONG"
        fun newInstance(song: Song?): ListSongsFragment{
            val fragment = ListSongsFragment()
            val bundle = Bundle().apply {
                putSerializable(ARG_SONG, song)
            }
            Log.d("bundle", bundle.size().toString() + "bytes")
            fragment.arguments = bundle
            return fragment
        }
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentListSongsBinding.inflate(inflater, container, false)
        val song = arguments?.getSerializable(ARG_SONG) as Song?
        setRecyclerview(song)
        setSearchBar()
        setEvent()
        return binding.root
    }

    private fun setSearchBar() {
        binding.edtSearch.setOnEditorActionListener { textView, id, keyEvent ->
            if(id == EditorInfo.IME_ACTION_SEARCH){
                adapter.filter.filter(textView.text)
            }
            true
        }

        binding.edtSearch.addTextChangedListener(object : TextWatcher{
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) { }

            override fun onTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
                adapter.filter.filter(p0)
            }

            override fun afterTextChanged(p0: Editable?) { }

        })

        binding.edtSearch.setOnFocusChangeListener { view, b ->
            if(!b){
                adapter.filter.filter("")
                binding.edtSearch.text?.clear()
            }
        }
    }

    private fun setRecyclerview(song: Song?){
        val thread = Thread{ run{
            adapter = ListSongsAdapter(requireContext())

            adapter.onItemClick = {
                if(adapter.currentPosition == -1)
                    startMusicService(adapter.listSong, it)
                else if(mainActivity.service.song != adapter.listSong[it])
                    startMusicService(adapter.listSong, it)
                else{
                    val intent = Intent(requireContext(), MusicPlayerActivity::class.java)
                    startActivity(intent)
                }
            }

            adapter.actionMore = {
                val bottomMenu = BottomMenuFragment.newInstance(it,
                    if(mainActivity.service.isInitialized)
                        mainActivity.service.song.style
                    else
                        null
                )
                bottomMenu.show(parentFragmentManager, bottomMenu.tag)
            }
            adapter.listSong = ListSongManager.listSong.toMutableList()
            adapter.listSongOld = ListSongManager.listSong.toMutableList()
            setStyle(song)
            binding.rcvListSong.post{
                binding.rcvListSong.adapter = adapter
            }
        } }
        thread.start()
    }

    private fun setEvent(){
        binding.imgMenu.setOnClickListener {
            showMenu(it)
        }
    }

    private fun showMenu(v: View) {
        val listener = PopupMenu.OnMenuItemClickListener { menuItem ->
            when(menuItem.itemId){
                R.id.sort_by_name -> PreferencesManager(requireContext()).sort = 0
                R.id.sort_by_date -> PreferencesManager(requireContext()).sort = 1
                R.id.sort_by_duration -> PreferencesManager(requireContext()).sort = 2
                R.id.sort_by_quality -> PreferencesManager(requireContext()).sort = 3
                R.id.sort_by_size -> PreferencesManager(requireContext()).sort = 4
                R.id.sort_asc -> PreferencesManager(requireContext()).sortType = 0
                R.id.sort_des -> PreferencesManager(requireContext()).sortType = 1
            }
            adapter.listSongOld.apply {
                when(PreferencesManager(requireContext()).sortType){
                    0 -> { when(PreferencesManager(requireContext()).sort){
                        0 -> sortBy { it.title.first() }
                        1 -> sortBy { it.date }
                        2 -> sortBy { it.duration }
                        3 -> sortBy { it.bitrate }
                        4 -> sortBy { it.fileSize }
                    } }
                    1 -> { when(PreferencesManager(requireContext()).sort){
                        0 -> sortByDescending { it.title.first() }
                        1 -> sortByDescending { it.date }
                        2 -> sortByDescending { it.duration }
                        3 -> sortByDescending { it.bitrate }
                        4 -> sortByDescending { it.fileSize }
                    } }
                }
            }
            if(adapter.currentPosition > -1) {
                val song = adapter.listSong[adapter.currentPosition]
                adapter.currentPosition =  adapter.listSongOld.indexOf(song)
                Log.d("currentPosition", adapter.currentPosition.toString())
            }
            adapter.listSong = adapter.listSongOld.toMutableList()
            adapter.notifyDataSetChanged()
            true
        }
        PopupMenu(requireContext(), v).apply {
            inflate(R.menu.menu_list_song)
            setOnMenuItemClickListener(listener)
            gravity = Gravity.END
            show()
        }
    }

    fun setStyle(song: Song?){
        val position = adapter.listSong.indexOf(song)
        adapter.setStyle(song?.style, position)
        song?.style?.let {
            binding.toolbar.setBackgroundColor(it.backgroundColor)
            DrawableCompat.setTint(binding.imgMenu.drawable, it.contentColor)
            binding.tvAppName.setTextColor(it.contentColor)
            binding.edtSearch.setTextColor(it.contentColor)
            binding.edtSearch.setHintTextColor(it.bodyTextColor)
            TextViewCompat.setCompoundDrawableTintList(binding.edtSearch, it.stateList)
        }
    }

    private fun startMusicService(_listSong: List<Song>, _position: Int){
        val intent = Intent(requireContext(), MusicPlayerService::class.java)
        activity?.startService(intent)
        with(mainActivity.service){
            playListName = "Tất cả bản nhạc"
            listSong = _listSong.toMutableList()
            position = _position
            startMusic()
        }
    }
}