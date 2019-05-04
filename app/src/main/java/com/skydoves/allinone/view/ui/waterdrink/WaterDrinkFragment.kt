/*
 * Copyright (C) 2019 skydoves
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.skydoves.allinone.view.ui.waterdrink

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.github.jorgecastillo.FillableLoader
import com.github.jorgecastillo.FillableLoaderBuilder
import com.github.jorgecastillo.clippingtransforms.WavesClippingTransform
import com.skydoves.allinone.R
import com.skydoves.allinone.extension.observeLiveData
import com.skydoves.allinone.extension.vm
import com.skydoves.allinone.utils.FillAbleLoaderPaths
import com.skydoves.allinone.utils.FillAbleLoaderUtils
import com.skydoves.allinone.utils.WaterDrinkItemUtils
import com.yalantis.guillotine.animation.GuillotineAnimation
import dagger.android.support.AndroidSupportInjection
import kotlinx.android.synthetic.main.layout_waterdrink.*
import org.jetbrains.anko.support.v4.startActivity
import javax.inject.Inject


class WaterDrinkFragment : Fragment() {

  @Inject
  lateinit var viewModelFactory: ViewModelProvider.Factory
  private val viewModel by lazy { vm(viewModelFactory, WaterDrinkViewModel::class) }

  private lateinit var fillAbleLoader: FillableLoader
  private lateinit var layoutMenu: GuillotineAnimation

  override fun onAttach(context: Context) {
    AndroidSupportInjection.inject(this)
    super.onAttach(context)
  }

  override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
    return inflater.inflate(R.layout.layout_waterdrink, container, false)
  }

  override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
    super.onViewCreated(view, savedInstanceState)
    initializeUI()
    observeLiveData()
  }

  @SuppressLint("SetTextI18n")
  private fun initializeUI() {
    context?.let {
      val loaderBuilder = FillableLoaderBuilder()
      this.fillAbleLoader = loaderBuilder.parentView(parentView)
          .svgPath(FillAbleLoaderPaths.SVG_WATERDROP)
          .layoutParams(FillAbleLoaderUtils.getParams(it))
          .originalDimensions(290, 425)
          .fillColor(ContextCompat.getColor(it, R.color.waterBlue))
          .strokeColor(ContextCompat.getColor(it, R.color.waterBlue))
          .strokeDrawingDuration(0)
          .clippingTransform(WavesClippingTransform())
          .fillDuration(3000)
          .build()

      percentage.bringToFront()
    }

    inflateGraphLayout()
    more.setOnClickListener { layoutMenu.open() }

    goal.text = "${viewModel.getWaterGoal()} ml"
    fab_drink.setOnClickListener { startActivity<WaterDrinkSelectActivity>() }
  }

  @SuppressLint("InflateParams")
  private fun inflateGraphLayout() {
    val inflater = activity?.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater
    val chartLayout = inflater.inflate(R.layout.layout_water_drink_graph, null)
    parent.addView(chartLayout)
    this.layoutMenu = GuillotineAnimation.GuillotineBuilder(chartLayout, chartLayout.findViewById(R.id.more), more)
        .build()
    this.layoutMenu.close()
  }

  @SuppressLint("SetTextI18n")
  private fun observeLiveData() {
    observeLiveData(viewModel.getTodayWaterDrinks()) {
      val todayAmounts = WaterDrinkItemUtils.getWaterAmouts(it)
      drink_today.text = "$todayAmounts ml"
      val should = viewModel.getWaterGoal() - todayAmounts
      if (should > 0) {
        drink_should.text = "$should ml"
      } else {
        drink_should.text = "0ml"
      }
      val percent = (todayAmounts.toFloat() / viewModel.getWaterGoal().toFloat() * 100)
      if (percent < 100) {
        percentage.text = "${percent.toInt()}%"
      } else {
        percentage.text = "100%"
      }
      fillAbleLoader.reset()
      fillAbleLoader.setSvgPath(FillAbleLoaderPaths.SVG_WATERDROP)
      fillAbleLoader.setFillColor(Color.WHITE)
      fillAbleLoader.setPercentage(percent)
      fillAbleLoader.start()
    }
  }
}
