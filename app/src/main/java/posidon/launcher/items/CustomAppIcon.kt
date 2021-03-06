package posidon.launcher.items

import android.content.Context
import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroup.LayoutParams.MATCH_PARENT
import android.widget.*
import androidx.appcompat.app.AppCompatActivity
import posidon.launcher.Main
import posidon.launcher.R
import posidon.launcher.search.SearchActivity
import posidon.launcher.storage.Settings
import posidon.launcher.tools.ThemeTools
import posidon.launcher.tools.Tools
import posidon.launcher.tools.dp

class CustomAppIcon : AppCompatActivity() {

    private val iconPacks = ArrayList<App>()
    private lateinit var app: String
    private lateinit var gridView: GridView
    private lateinit var defaultOption: View
    private lateinit var searchBar: EditText
    private var state = 0

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        gridView = GridView(this)
        setContentView(LinearLayout(this).apply {
            val li = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            addView(li.inflate(R.layout.list_item, null).apply {
                try { findViewById<ImageView>(R.id.iconimg).setImageDrawable(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Tools.adaptic(packageManager.getApplicationIcon("com.android.systemui"))
                } else {
                    packageManager.getApplicationIcon("com.android.systemui")
                }) }
                catch (ignore: Exception) {}
                findViewById<TextView>(R.id.icontxt).text = "Default"
                setOnClickListener {
                    Settings["app:$app:icon"] = ""
                    finish()
                }
                defaultOption = this
            })
            addView(EditText(this@CustomAppIcon).apply {
                hint = "Search icons"
                addTextChangedListener(object : TextWatcher {
                    override fun afterTextChanged(s: Editable?) {}
                    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
                    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {
                        (gridView.adapter as IconsAdapter).search(s.toString())
                    }
                })
                setPadding(18.dp.toInt(), 0, 18.dp.toInt(), 0)
                layoutParams = ViewGroup.LayoutParams(MATCH_PARENT, 64.dp.toInt())
                setBackgroundColor(0xdd111213.toInt())
                visibility = View.GONE
                searchBar = this
            })
            addView(gridView)
            orientation = LinearLayout.VERTICAL
        })
        window.decorView.setBackgroundColor(0x55111213)

        app = intent.extras!!.getString("packageName", null)

        val mainIntent = Intent(Intent.ACTION_MAIN, null)
        mainIntent.addCategory("com.anddoes.launcher.THEME")
        val pacslist = packageManager.queryIntentActivities(mainIntent, 0)
        for (i in pacslist.indices) {
            iconPacks.add(App(pacslist[i].activityInfo.packageName))
            iconPacks[i].icon = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                Tools.adaptic(pacslist[i].loadIcon(packageManager))
            } else {
                pacslist[i].loadIcon(packageManager)
            }
            iconPacks[i].label = pacslist[i].loadLabel(packageManager).toString()
        }

        gridView.adapter = IconpacksAdapter()
        gridView.setOnItemClickListener { _,_,i,_ -> try {
            gridView.numColumns = 4
            gridView.adapter = IconsAdapter(iconPacks[i].packageName)
            state = 1
            defaultOption.visibility = View.GONE
            searchBar.visibility = View.VISIBLE
        } catch (e: Exception) {}}
    }

    internal inner class IconpacksAdapter : BaseAdapter() {

        override fun getCount(): Int = iconPacks.size
        override fun getItem(position: Int): Any? = null
        override fun getItemId(position: Int): Long = 0

        inner class ViewHolder(val icon: ImageView, val text: TextView)

        override fun getView(position: Int, cv: View?, parent: ViewGroup): View? {
            var convertView = cv
            val viewHolder: ViewHolder
            val li = getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
            if (convertView == null) {
                convertView = li.inflate(R.layout.list_item, null)
                viewHolder = ViewHolder(
                    convertView.findViewById(R.id.iconimg),
                    convertView.findViewById(R.id.icontxt))
                convertView.tag = viewHolder
            } else viewHolder = convertView.tag as ViewHolder
            viewHolder.icon.setImageDrawable(iconPacks[position].icon)
            viewHolder.text.text = iconPacks[position].label
            viewHolder.text.visibility = View.VISIBLE
            viewHolder.text.setTextColor(Settings["labelColor", -0x11111112])
            var appSize = 0
            when (Settings["icsize", 1]) {
                0 -> appSize = 64.dp.toInt()
                1 -> appSize = 74.dp.toInt()
                2 -> appSize = 84.dp.toInt()
            }
            viewHolder.icon.layoutParams.height = appSize
            viewHolder.icon.layoutParams.width = appSize
            return convertView
        }
    }

    internal inner class IconsAdapter(val iconPack: String) : BaseAdapter() {

        val icons: ArrayList<String>
        val themeRes = packageManager.getResourcesForApplication(iconPack)
        val searchResults = ArrayList<String>()

        init {
            icons = try { ThemeTools.getResourceNames(themeRes, iconPack) }
                    catch (e: Exception) { ArrayList() }
            searchResults.addAll(icons)
        }

        fun search(term: String) {
            searchResults.clear()
            for (string in icons) {
                if (SearchActivity.searchOptimize(string).contains(SearchActivity.searchOptimize(term))) {
                    searchResults.add(string)
                }
            }
            notifyDataSetChanged()
        }

        override fun getCount(): Int = searchResults.size
        override fun getItem(position: Int): Any? = null
        override fun getItemId(position: Int): Long = 0

        inner class ViewHolder(val icon: ImageView)

        override fun getView(i: Int, cv: View?, parent: ViewGroup): View? {
            var convertView = cv
            val viewHolder: ViewHolder
            if (convertView == null) {
                convertView = LayoutInflater.from(this@CustomAppIcon).inflate(R.layout.drawer_item, null)
                viewHolder = ViewHolder(convertView.findViewById(R.id.iconimg))
                convertView.findViewById<View>(R.id.icontxt).visibility = View.GONE
                viewHolder.icon.layoutParams.run {
                    val appSize = when (Settings["icsize", 1]) {
                        0 -> 64.dp.toInt()
                        2 -> 84.dp.toInt()
                        else -> 74.dp.toInt()
                    }
                    width = appSize
                    height = appSize
                }
                convertView.tag = viewHolder
            } else {
                viewHolder = convertView.tag as ViewHolder
            }

            val intRes = themeRes.getIdentifier(searchResults[i], "drawable", iconPack)
            if (intRes != 0) {
                viewHolder.icon.setImageDrawable(Tools.tryAnimate(if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                    Tools.adaptic(themeRes.getDrawable(intRes))
                } else {
                    themeRes.getDrawable(intRes)
                }))
                viewHolder.icon.setOnClickListener {
                    Settings["app:$app:icon"] = "ref:$iconPack|${searchResults[i]}"
                    Main.shouldSetApps = true
                    finish()
                }
            }
            return convertView
        }
    }

    override fun onBackPressed() {
        when (state) {
            0 -> super.onBackPressed()
            1 -> {
                state = 0
                gridView.numColumns = 1
                searchBar.setText("")
                gridView.adapter = IconpacksAdapter()
                defaultOption.visibility = View.VISIBLE
                searchBar.visibility = View.GONE
            }
        }
    }
}