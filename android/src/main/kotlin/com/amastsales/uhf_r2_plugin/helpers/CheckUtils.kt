package com.amastsales.uhf_r2_plugin.helpers

import com.rscja.deviceapi.entity.UHFTAGInfo;

class CheckUtils {

    companion object {
        fun getInsertIndex( listData: List<UHFTAGInfo>, newInfo: UHFTAGInfo, exists: BooleanArray): Int {
            var startIndex: Int = 0
            var endIndex: Int = listData.size
            var judgeIndex: Int
            var ret: Int

            if (endIndex == 0){
                exists[0] = false
                return 0
            }

            endIndex --

            while (true){
                judgeIndex = (startIndex + endIndex) / 2
                ret = compareBytes(newInfo.getEpcBytes(), listData.get(judgeIndex).getEpcBytes())
                if (ret > 0){
                    if (judgeIndex == endIndex){
                        exists[0]=false
                        return judgeIndex+1
                    }
                    startIndex = judgeIndex+1
                } else if (ret < 0){
                    if (judgeIndex == startIndex){
                        exists[0]=false
                        return judgeIndex
                    }
                    endIndex = judgeIndex-1
                } else {
                    exists[0]=true
                    return judgeIndex
                }
            }
        }

        private fun compareBytes( b1: ByteArray, b2: ByteArray): Int {
            var len: ByteArray = if (b1.size < b2.size) b1 else b2
            var value1: Int
            var value2: Int

            for ((i, value) in len.withIndex()) {
                value1 = b1[i].toInt() and 0xFF
                value2 = b2[i].toInt() and 0xFF

                if (value1 > value2){
                    return 1
                } else if (value1 < value2){
                    return -1
                }
            }

            if (b1.size > b2.size){
                return 2
            } else if (b1.size < b2.size){
                return -2
            }else {
                return 0;
            }
        }

    }
}