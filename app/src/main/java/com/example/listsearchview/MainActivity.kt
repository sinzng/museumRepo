package com.example.listsearchview

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.SearchView
import android.widget.SearchView.OnQueryTextListener
import com.example.listsearchview.databinding.ActivityMainBinding
import java.io.BufferedReader
import java.io.InputStream
import java.io.InputStreamReader
import android.content.Context
import android.widget.Filter
import android.widget.Filterable

class CustomAdapter(context: Context, private val originalList: List<String>) :
    ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, originalList), Filterable {

    private val filteredList = mutableListOf<String>()

    init {
        filteredList.addAll(originalList)
    }

    override fun getCount(): Int {
        return filteredList.size.coerceAtMost(5) // 최대 10개의 아이템만 보여지도록 수정
    }

    override fun getItem(position: Int): String? {
        return filteredList[position]
    }

    override fun getFilter(): Filter {
        return object : Filter() {
            override fun performFiltering(constraint: CharSequence?): FilterResults {
                val results = FilterResults()
                if (constraint.isNullOrEmpty()) {
                    results.values = originalList
                    results.count = originalList.size
                } else {
                    val filterPattern = constraint.toString().trim().toLowerCase()
                    val filteredItems = originalList.filter { item ->
                        val firstColumn = item.split(",")[0].trim().toLowerCase()
                        firstColumn.contains(filterPattern)
                    }
                    results.values = filteredItems
                    results.count = filteredItems.size.coerceAtMost(10) // 최대 10개의 아이템만 보여지도록 수정
                }
                return results
            }

            override fun publishResults(constraint: CharSequence?, results: FilterResults) {
                filteredList.clear()
                filteredList.addAll(results.values as List<String>)
                notifyDataSetChanged()
            }
        }
    }
}


    class MainActivity : AppCompatActivity() {

        lateinit var binding: ActivityMainBinding
        lateinit var userAdapter: CustomAdapter
        val fullUserList = mutableListOf<String>() // 전체 데이터 리스트

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val inputStream: InputStream = assets.open("list.csv")
        val bufferedReader = BufferedReader(InputStreamReader(inputStream))

        bufferedReader.use { reader ->
            var line: String?
            var isFirstRow = true
            while (reader.readLine().also { line = it } != null) {
                if (isFirstRow) {
                    isFirstRow = false
                    continue // 첫 번째 행은 건너뜀
                }
                val items = line?.split(",") // 쉼표로 데이터를 분리
                if (items?.isNotEmpty() == true) {
                    fullUserList.add(items[0]) // 첫 번째 컬럼의 데이터만 fullUserList에 추가
                }
            }
        }

        userAdapter = CustomAdapter(this, fullUserList)
        binding.listView.adapter = userAdapter

        binding.searchView.setOnQueryTextListener(object : OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                binding.searchView.clearFocus()
                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                userAdapter.filter.filter(newText)
                return false
            }
        })
    }
}
