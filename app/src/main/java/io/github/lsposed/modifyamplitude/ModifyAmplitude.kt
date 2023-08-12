package io.github.lsposed.modifyamplitude

import android.util.Log
import de.robv.android.xposed.IXposedHookLoadPackage
import de.robv.android.xposed.XC_MethodHook
import de.robv.android.xposed.XposedBridge
import de.robv.android.xposed.XposedHelpers
import de.robv.android.xposed.callbacks.XC_LoadPackage
import java.lang.reflect.Member
import java.lang.reflect.Method

class ModifyAmplitude :IXposedHookLoadPackage{
    companion object{
        var deoptimizeMethod: Method? = null
        var m:Method?=null
        fun static(){
            try {
                m = XposedBridge::class.java.getDeclaredMethod("deoptimizeMethod", Member::class.java)
            }catch (t:Throwable){
                XposedBridge.log(t)
            }
            deoptimizeMethod = m
        }
        fun deoptimizeMethod(c:Class<*>, n: String) {
            for (m in c::class.java.declaredMethods) {
                if (deoptimizeMethod != null && m.name.equals(n))
                    deoptimizeMethod!!.invoke(null, m)
                    Log.d("ModifyAmplitude", "ModifyAmplitude" + m)
            }
        }

    }

    override fun handleLoadPackage(lpparam: XC_LoadPackage.LoadPackageParam?) {
        if (lpparam != null) {
            if(lpparam.packageName.equals("android"))
                try {
                    val vibrationEffectClass: Class<*>  = XposedHelpers.findClass("android.os.VibrationEffect", lpparam!!.classLoader)
                    val createWaveformMethod = XposedHelpers.findAndHookMethod(vibrationEffectClass,"createWaveform", LongArray::class.java, IntArray::class.java, Int::class.java,object : XC_MethodHook() {
                        override fun beforeHookedMethod(param: MethodHookParam?) {
                            val amplitudes = param?.args?.get(1) as IntArray
                            for (i in 0 until amplitudes.size) {
                                amplitudes[i] = amplitudes[i].coerceAtMost(80)
                            }
                            param.args[1] = amplitudes
                            XposedBridge.log("ModifyAmplitude: ${amplitudes.contentToString()}")
                        }
                    })
                }catch (t: Throwable){
                        XposedBridge.log(t)
                }
        }

    }
}