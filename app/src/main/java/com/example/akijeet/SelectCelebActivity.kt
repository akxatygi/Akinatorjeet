package com.example.akijeet

import android.content.Intent
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.akijeet.data.Celebrity
import com.example.akijeet.game.GameManager

class SelectCelebActivity : AppCompatActivity() {

    private lateinit var searchEditText: EditText
    private lateinit var celebRecyclerView: RecyclerView
    private lateinit var adapter: CelebrityAdapter

    private var allCelebrities: List<Celebrity> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_select_celeb)

        searchEditText = findViewById(R.id.searchEditText)
        celebRecyclerView = findViewById(R.id.celebRecyclerView)

        allCelebrities = GameManager.getAllCelebrities(this)

        adapter = CelebrityAdapter(allCelebrities) { selectedCeleb ->
            onCelebritySelected(selectedCeleb)
        }

        celebRecyclerView.layoutManager = LinearLayoutManager(this)
        celebRecyclerView.adapter = adapter

        searchEditText.addTextChangedListener(object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {
                adapter.filter(s.toString())
            }
            override fun afterTextChanged(s: Editable?) {}
        })
    }

    private fun onCelebritySelected(celebrity: Celebrity) {
        GameManager.initGame(this, targetMode = true, target = celebrity)
        val intent = Intent(this, QuestionActivity::class.java)
        startActivity(intent)
        overridePendingTransition(R.anim.fade_in, R.anim.fade_out)
        finish()
    }

    private class CelebrityAdapter(
        private val originalList: List<Celebrity>,
        private val onItemClick: (Celebrity) -> Unit
    ) : RecyclerView.Adapter<CelebrityAdapter.ViewHolder>() {

        private var filteredList: List<Celebrity> = originalList

        fun filter(query: String) {
            filteredList = if (query.isEmpty()) {
                originalList
            } else {
                originalList.filter { it.name.contains(query, ignoreCase = true) }
            }
            notifyDataSetChanged()
        }

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
            val view = LayoutInflater.from(parent.context)
                .inflate(R.layout.item_celebrity, parent, false)
            return ViewHolder(view)
        }

        override fun onBindViewHolder(holder: ViewHolder, position: Int) {
            val celeb = filteredList[position]
            holder.nameText.text = celeb.name
            holder.itemView.setOnClickListener { onItemClick(celeb) }
        }

        override fun getItemCount(): Int = filteredList.size

        class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
            val nameText: TextView = itemView.findViewById(R.id.celebrityNameText)
        }
    }
}
