package com.example.multi_compass

import android.content.Context
import android.hardware.Sensor
import android.hardware.SensorEvent
import android.hardware.SensorEventListener
import android.hardware.SensorManager
import android.support.v7.app.AppCompatActivity
import android.os.Bundle
import android.widget.TextView
import kotlin.math.PI
import kotlin.math.atan
import kotlin.math.pow
import kotlin.math.sqrt
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity(), SensorEventListener {

    private var sensorManager: SensorManager by Delegates.notNull<SensorManager>()
    //private var mSensor: Sensor by Delegates.notNull<Sensor>()
    private var textView: TextView by Delegates.notNull<TextView>()
    private var textInfo: TextView by Delegates.notNull<TextView>()

    private var thetaX: Double = 0.0
    private var thetaY: Double = 0.0

    private var canvasDraw: CanvasDraw by Delegates.notNull<CanvasDraw>()

    private var accelerometerValues = FloatArray(3)
    private var geomagneticMatrix = FloatArray(3)
    var sensorReady: Boolean = false


    //private var roll = findViewById(R.id.roll) as TextView

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        //センサーマネージャーを取得する
        sensorManager = getSystemService(Context.SENSOR_SERVICE) as SensorManager
        //加速度計のセンサーを取得する
        //その他のセンサーを取得する場合には引数を違うものに変更する
        textInfo = findViewById(R.id.text_info)
        // Get an instance of the TextView
        textView = findViewById(R.id.text_view)
        canvasDraw = CanvasDraw(this, null)
        setContentView(canvasDraw)
    }

    override fun onAccuracyChanged(p0: Sensor?, p1: Int) {
        //センサーの精度が変化した時に呼び出されるメソッド
    }

    override fun onSensorChanged(event: SensorEvent?) {
        //センサーに何かしらのイベントが発生した時に呼ばれる
        val sensorX: Float
        val sensorY: Float
        val sensorZ: Float

        if (event?.sensor!!.type == Sensor.TYPE_ACCELEROMETER) {
            sensorX = event.values!![0]/9.8F
            sensorY = event.values!![1]/9.8F
            sensorZ = event.values!![2]/9.8F

            thetaX = atan(sensorX/ sqrt(2f.pow(sensorY)+2f.pow(sensorZ)))*180f/PI
            thetaY = atan(sensorY/ sqrt(2f.pow(sensorX)+2f.pow(sensorZ)))*180f/PI

            /*val strTmp = ("加速度センサー\n"
                    + " X: " + thetaX + "\n"
                    + " Y: " + thetaY)
            textView.text = strTmp*/
            accelerometerValues = event.values!!.clone()
            canvasDraw.setPosition(thetaX.toFloat(), thetaY.toFloat())
        }
        if(event.sensor!!.type == Sensor.TYPE_MAGNETIC_FIELD) {
            geomagneticMatrix = event.values!!.clone()
            sensorReady = true
        }

        if (!sensorReady) {
            return
        }
        sensorReady = false

        val R = FloatArray(16)
        val I = FloatArray(16)

        SensorManager.getRotationMatrix(R, I, accelerometerValues, geomagneticMatrix)

        val actualOrientation = FloatArray(3)
        SensorManager.remapCoordinateSystem(R, SensorManager.AXIS_X, SensorManager.AXIS_Y, R)
        SensorManager.getOrientation(R, actualOrientation)

        canvasDraw.setDirection((-1f)*actualOrientation[0])

    }

    //アクティビティが閉じられたときにリスナーを解除する
    override fun onPause() {
        super.onPause()
        //リスナーを解除しないとバックグラウンドにいるとき常にコールバックされ続ける
        sensorManager.unregisterListener(this)
    }

    override fun onResume() {
        super.onResume()
        val acceleration: Sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER)
        val compass: Sensor = sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD)
        //リスナーとセンサーオブジェクトを渡す
        //第一引数はインターフェースを継承したクラス、今回はthis
        //第二引数は取得したセンサーオブジェクト
        //第三引数は更新頻度 UIはUI表示向き、FASTはできるだけ早く、GAMEはゲーム向き
        sensorManager.registerListener(this, acceleration, SensorManager.SENSOR_DELAY_UI)
        sensorManager.registerListener(this, compass, SensorManager.SENSOR_DELAY_UI)
    }

    // （お好みで）加速度センサーの各種情報を表示
    /*private fun showInfo(event: SensorEvent) {
        // センサー名
        val info = StringBuffer("Name: ")
        info.append(event.sensor.name)
        info.append("\n")

        // ベンダー名
        info.append("Vendor: ")
        info.append(event.sensor.vendor)
        info.append("\n")

        // 型番
        info.append("Type: ")
        info.append(event.sensor.type)
        info.append("\n")

        // 最小遅れ
        var data = event.sensor.minDelay
        info.append("Mindelay: ")
        info.append(data.toString())
        info.append(" usec\n")

        // 最大遅れ
        data = event.sensor.maxDelay
        info.append("Maxdelay: ")
        info.append(data.toString())
        info.append(" usec\n")

        // レポートモード
        data = event.sensor.reportingMode
        var stinfo = "unknown"
        if (data == 0) {
            stinfo = "REPORTING_MODE_CONTINUOUS"
        } else if (data == 1) {
            stinfo = "REPORTING_MODE_ON_CHANGE"
        } else if (data == 2) {
            stinfo = "REPORTING_MODE_ONE_SHOT"
        }
        info.append("ReportingMode: ")
        info.append(stinfo)
        info.append("\n")

        // 最大レンジ
        info.append("MaxRange: ")
        var fData = event.sensor.maximumRange
        info.append(fData.toString())
        info.append("\n")

        // 分解能
        info.append("Resolution: ")
        fData = event.sensor.resolution
        info.append(fData.toString())
        info.append(" m/s^2\n")

        // 消費電流
        info.append("Power: ")
        fData = event.sensor.power
        info.append(fData.toString())
        info.append(" mA\n")

        textInfo.text = info
    }*/
}

