/*
 * Copyright 2021 The Android Open Source Project
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.example.androiddevchallenge.vm

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.androiddevchallenge.MyTimer
import com.example.androiddevchallenge.utils.GreenProgress
import com.example.androiddevchallenge.utils.RedProgress
import com.example.androiddevchallenge.utils.YellowProgress
import com.example.androiddevchallenge.utils.toFormattedTime
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class MainViewModel : ViewModel() {
    private var countDownTimer: MyTimer? = null

    private val mTime = MutableLiveData((0L).toFormattedTime())
    val time: LiveData<String> = mTime

    private val mProgress = MutableLiveData(0.00F)
    val progress: LiveData<Float> = mProgress

    private val mIsRunning = MutableLiveData(false)
    val isRunning: LiveData<Boolean> = mIsRunning

    private val mLastTenSeconds = MutableLiveData(false)
    val lastTenSeconds: LiveData<Boolean> = mLastTenSeconds

    private val mProgressColor = MutableLiveData(GreenProgress)
    val progressColor: LiveData<Long> = mProgressColor

    private val mTimeQuotes = MutableLiveData("")
    val timeQuotes: LiveData<String> = mTimeQuotes
    private var currentIndex = 0

    fun startTimer(timeInMs: Long = 1000L) {
        mIsRunning.value = true
        mProgressColor.value = GreenProgress
        mProgress.value = 1f
        mTime.value = (timeInMs).toFormattedTime()

        viewModelScope.launch {
            while (isActive && isRunning.value == true) {

                currentIndex += 1
                if (currentIndex >= quotesList().size)
                    currentIndex = 0

                mTimeQuotes.postValue(quotesList().get(currentIndex))

                delay(12000)
            }
        }

        countDownTimer = object : MyTimer(timeInMs, 1000) {
            override fun onTick(timeLeft: Long) {
                mProgressColor.postValue(YellowProgress)
                mProgress.postValue(timeLeft.toFloat() / timeInMs)
                mTime.postValue(timeLeft.toFormattedTime())

                if (timeLeft <= 10_000) {
                    mLastTenSeconds.postValue(true)
                }
            }

            override fun onFinished() {
                mIsRunning.postValue(false)
                mProgress.postValue(0f)
                mTime.postValue((0L).toFormattedTime())
            }
        }

        viewModelScope.launch {
            delay(500)
            countDownTimer?.start()
        }
    }

    fun cancelTimer() {
        countDownTimer?.cancel()

        mProgressColor.value = RedProgress
        mIsRunning.value = false
        mProgress.value = 0f
        mTime.value = (0L).toFormattedTime()
    }

    fun quotesList() = listOf(
        "静夜思",
        "窗前明月光",
        "疑是地上霜",
        "举头望明月",
        "低头思故乡",
    )
}
