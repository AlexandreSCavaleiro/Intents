package br.edu.scl.ifsp.ads.intents

import android.Manifest.permission.CALL_PHONE
import android.content.Intent
import android.content.Intent.ACTION_CALL
import android.content.Intent.ACTION_DIAL
import android.content.Intent.ACTION_PICK
import android.content.Intent.ACTION_VIEW
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.Environment
import android.view.Menu
import android.view.MenuItem
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import br.edu.scl.ifsp.ads.intents.databinding.ActivityMainBinding

class MainActivity : AppCompatActivity() {
    private val amb: ActivityMainBinding by lazy {
        ActivityMainBinding.inflate(layoutInflater)
    }
    private lateinit var parl:ActivityResultLauncher<Intent>
    private lateinit var permissaoChamadaArl: ActivityResultLauncher<String>
    private lateinit var pegarImageArl: ActivityResultLauncher<Intent>

    companion object{
        const val PARAMETRO_EXTRA = "PARAMETRO_EXTRA"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(amb.root)
        supportActionBar?.subtitle="MainActivity"

        parl =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result?.resultCode == RESULT_OK) {

                    result.data?.getStringExtra(PARAMETRO_EXTRA)?.let { parametro ->
                        amb.parametroTv.text = parametro
                    }
                }
            }

        permissaoChamadaArl = registerForActivityResult(ActivityResultContracts.RequestPermission()){
            permissaoConcedida ->
            if (permissaoConcedida){
                chamarNumero(true)
            }
            else{
                Toast.makeText(this, "Sem permissão, sem chamada!", Toast.LENGTH_SHORT).show()
            }
        pegarImageArl = registerForActivityResult(ActivityResultContracts.StartActivityForResult()){
            result ->
            if(result.resultCode == RESULT_OK){
                val imagemUri = result.data?.data
                imagemUri?.let {
                    amb.parametroTv.text = imagemUri.toString()
                    val visualizarIntent = Intent(ACTION_VIEW, imagemUri)
                    startActivity(visualizarIntent)
                }
            }
        }
        }

        amb.entrarParametroBt.setOnClickListener {
            val parametroIntent = Intent("PARAMETRO_ACTIVITY_ACTION")
            parametroIntent.putExtra(PARAMETRO_EXTRA, amb.parametroTv.text.toString())

            parl.launch(parametroIntent)
        }
    }

    //segunda aula nesse codigo
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when(item.itemId) {
            R.id.viewMi -> {
                val url: Uri = Uri.parse(amb.parametroTv.text.toString())
                val navegadorIntent = Intent(ACTION_VIEW, url)
                startActivity(navegadorIntent)
                true
            }
            R.id.callMi -> {
                if(Build.VERSION.SDK_INT>= Build.VERSION_CODES.M){
                    if (checkSelfPermission(CALL_PHONE) == PackageManager.PERMISSION_GRANTED){
                        chamarNumero(true);
                    } else{
                        permissaoChamadaArl.launch(CALL_PHONE)
                    }
                }else {
                    chamarNumero(true)
                }
                true
            }
            R.id.dialMi -> {
                chamarNumero(false)
                true
            }
            R.id.pickMi ->{
                val pegarImagemIntent = Intent(ACTION_PICK)
                val diretorioImagens = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_PICTURES).path
                pegarImagemIntent.setDataAndType(Uri.parse(diretorioImagens), "image/*")
                pegarImageArl.launch(pegarImagemIntent);
                true
            }
            R.id.chooserMi -> true
            else -> true
        }
    }
    private fun chamarNumero(chamar: Boolean){
        val numeroUri = Uri.parse("tel: ${amb.parametroTv.text}")
        val chamarIntent =Intent(
            if(chamar)ACTION_CALL else ACTION_DIAL)
        chamarIntent.data = numeroUri
        startActivity(chamarIntent)
    }

}