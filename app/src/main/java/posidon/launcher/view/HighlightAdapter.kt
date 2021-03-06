package posidon.launcher.view

import android.graphics.drawable.Drawable
import android.graphics.drawable.ShapeDrawable
import android.graphics.drawable.shapes.RoundRectShape
import posidon.launcher.Main
import posidon.launcher.tools.dp

interface HighlightAdapter {

    fun highlight(i: Int)
    fun unhighlight()

    companion object {
        fun createHighlightDrawable(): Drawable {
            val bg = ShapeDrawable()
            val r = 12.dp
            bg.shape = RoundRectShape(floatArrayOf(r, r, r, r, r, r, r, r), null, null)
            bg.paint.color = Main.accentColor and 0xffffff or 0x55000000
            return bg
        }
    }
}