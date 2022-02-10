package com.edvora.paintapp

import android.graphics.Paint
import android.graphics.Path
import android.os.Bundle
import android.transition.Slide
import android.transition.Transition
import android.transition.TransitionManager
import android.view.Gravity
import android.view.View
import android.view.ViewGroup
import android.view.ViewGroupOverlay
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.ImageButton
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.isVisible
import com.edvora.paintapp.PaintView.Companion.arrowPath
import com.edvora.paintapp.PaintView.Companion.currentBrush
import com.edvora.paintapp.PaintView.Companion.currentToolPaint
import com.edvora.paintapp.PaintView.Companion.ellipsePath
import com.edvora.paintapp.PaintView.Companion.pencilPath
import com.edvora.paintapp.PaintView.Companion.rectanglePath
import com.edvora.paintapp.databinding.ActivityMainBinding
import com.edvora.paintapp.databinding.ColorPaletteBinding
import com.edvora.paintapp.util.ToolsPaint
import com.edvora.paintapp.util.ToolsPaint.*


private lateinit var binding: ActivityMainBinding
private lateinit var colorPaletteBinding: ColorPaletteBinding
private var lastPaintToolUsed: ToolsPaint = Pencil
typealias ResourceId = R.id
typealias ResourceColor = R.color
typealias ResourceDrawable = R.drawable


class MainActivity : AppCompatActivity(), View.OnClickListener {
    companion object {
        var paintBrush = Paint()
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        val view = binding.root
        setContentView(view)
        lastPaintToolUsed = Pencil
        binding.buttonPencil.setOnClickListener(this)
        binding.buttonArrow.setOnClickListener(this)
        binding.buttonRectangle.setOnClickListener(this)
        binding.buttonEllipse.setOnClickListener(this)
        binding.buttonPalette.setOnClickListener(this)
        colorPaletteBinding = binding.colorPaletteLayout
        colorPaletteBinding.buttonRed.setOnClickListener(this)
        colorPaletteBinding.buttonGreen.setOnClickListener(this)
        colorPaletteBinding.buttonBlue.setOnClickListener(this)
        colorPaletteBinding.buttonBlack.setOnClickListener(this)
    }

    override fun onClick(view: View) {
        clearPaintToolsBackGround()
        when (view.id) {
            ResourceId.button_pencil -> {
                binding.buttonPencil.setPaintToolSelected()
                lastPaintToolUsed = Pencil
                currentToolPaint(Pencil)
            }
            ResourceId.button_arrow -> {
                binding.buttonArrow.setPaintToolSelected()
                lastPaintToolUsed = Arrow
                currentToolPaint(Arrow)
            }
            ResourceId.button_rectangle -> {
                binding.buttonRectangle.setPaintToolSelected()
                lastPaintToolUsed = Rectangle
                currentToolPaint(Rectangle)
            }
            ResourceId.button_ellipse -> {
                binding.buttonEllipse.setPaintToolSelected()
                lastPaintToolUsed = Ellipse
                currentToolPaint(Ellipse)
            }
            ResourceId.button_palette -> {
                binding.buttonPalette.setPaintToolSelected()
                colorPaletteBinding.root.flipVisibility()
            }
            ResourceId.button_red -> {
                paintBrush setColorTo ResourceColor.red
                currentColor(paintBrush.color)
                colorPaletteBinding.root.flipVisibility()
                lastToolUsed(lastPaintToolUsed).setPaintToolSelected()
                showToastColorSelected("red")
            }
            ResourceId.button_green -> {
                paintBrush setColorTo ResourceColor.green
                currentColor(paintBrush.color)
                colorPaletteBinding.root.flipVisibility()
                lastToolUsed(lastPaintToolUsed).setPaintToolSelected()
                showToastColorSelected("green")
            }
            ResourceId.button_blue -> {
                paintBrush setColorTo ResourceColor.blue
                currentColor(paintBrush.color)
                colorPaletteBinding.root.flipVisibility()
                lastToolUsed(lastPaintToolUsed).setPaintToolSelected()
                showToastColorSelected("blue")
            }
            ResourceId.button_black -> {
                paintBrush setColorTo ResourceColor.black
                currentColor(paintBrush.color)
                colorPaletteBinding.root.flipVisibility()
                showToastColorSelected("black")
            }
        }
    }

    private fun currentColor(color: Int) {
        currentBrush = color
        restPath()
    }

    private fun currentToolPaint(toolPaint: ToolsPaint) {
        currentToolPaint = toolPaint
        restPath()
    }

    private fun restPath() {
        pencilPath = Path()
        ellipsePath = Path()
        arrowPath = Path()
        rectanglePath = Path()
    }

    private fun clearPaintToolsBackGround() {
        binding.buttonPencil.clearPaintToolBackGround()
        binding.buttonArrow.clearPaintToolBackGround()
        binding.buttonRectangle.clearPaintToolBackGround()
        binding.buttonEllipse.clearPaintToolBackGround()
        binding.buttonPalette.clearPaintToolBackGround()
    }

    private fun ImageButton.clearPaintToolBackGround() {
        setBackgroundColor(
            ResourcesCompat.getColor(
                resources,
                ResourceColor.background_paint_tool_not_selected,
                null
            )
        )
        setColorFilter(
            ResourcesCompat.getColor(
                resources,
                ResourceColor.paint_tool_not_selected,
                null
            )
        )
    }

    private fun ImageButton.setPaintToolSelected() {
        background =
            ResourcesCompat.getDrawable(
                resources,
                ResourceDrawable.paint_tool_selected_background,
                null
            )

        setColorFilter(getColorFromResource(ResourceColor.black))
        if (this.id != binding.buttonPalette.id) {
            if (colorPaletteBinding.root.isVisible) {
                binding.buttonPalette.clearPaintToolBackGround()
                colorPaletteBinding.root.flipVisibility()
            }
        }
    }

    private fun View.flipVisibility() {
        val anim: Animation
        visibility = if (!isVisible) {
            anim = AnimationUtils.loadAnimation(this@MainActivity, R.anim.fade_in);
            View.VISIBLE
        } else {
            anim = AnimationUtils.loadAnimation(this@MainActivity, R.anim.fade_out);
            View.INVISIBLE
        }
        startAnimation(anim)
    }

    private fun lastToolUsed(lastPaintToolUsed: ToolsPaint): ImageButton {
        return when (lastPaintToolUsed) {
            Pencil -> binding.buttonPencil
            Rectangle -> binding.buttonRectangle
            Arrow -> binding.buttonArrow
            Ellipse -> binding.buttonEllipse
            else -> {
                binding.buttonPencil
            }
        }
    }

    private infix fun Paint.setColorTo(color: Int) {
        this.color = getColorFromResource(color)
    }


    private fun getColorFromResource(color: Int): Int {
        return ResourcesCompat.getColor(
            resources,
            color,
            null
        )
    }

    private fun showToastColorSelected(color: String) {
        Toast.makeText(this, "$color selected", Toast.LENGTH_SHORT).show()
    }

}