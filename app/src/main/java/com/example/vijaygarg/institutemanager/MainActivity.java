package com.example.vijaygarg.institutemanager;

import android.content.Context;
import android.os.AsyncTask;
import android.os.Environment;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import com.example.vijaygarg.institutemanager.Models.Student;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import java.io.File;
import java.io.FileInputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Iterator;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    EditText filename;
    Button  uploadbutton;
    ArrayList<Student> arrayList;
    DatabaseReference databaseReference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        filename=findViewById(R.id.filename);
        uploadbutton=findViewById(R.id.uploadfile);
        uploadbutton.setOnClickListener(this);
        arrayList=new ArrayList<>();

    }


    @Override
    public void onClick(View v) {
        int id=v.getId();
        switch (id){
            case R.id.uploadfile:
                String sfilename=filename.getText().toString();
                readExcelFile(MainActivity.this,sfilename);

                break;
        }
    }

    private void readExcelFile(Context context, String filename) {

        if (!isExternalStorageAvailable() || isExternalStorageReadOnly())
        {
            Log.w("FileUtils", "Storage not available or read only");
            return;
        }

        try{
            // Creating Input Stream
            File file = new File(context.getExternalFilesDir(null), filename);
            FileInputStream myInput = new FileInputStream(file);
            POIFSFileSystem myFileSystem = new POIFSFileSystem(myInput);
            HSSFWorkbook myWorkBook = new HSSFWorkbook(myFileSystem);

            HSSFSheet mySheet = myWorkBook.getSheetAt(0);
            Iterator<Row> rowIter = mySheet.rowIterator();
            rowIter.next();
            while(rowIter.hasNext()){
                HSSFRow myRow = (HSSFRow) rowIter.next();
                Iterator<Cell> cellIter = myRow.cellIterator();
                String values[]=new String[7];
                int i=0;
                while(cellIter.hasNext()){
                    HSSFCell myCell = (HSSFCell) cellIter.next();
                    values[i]=myCell.toString();
                    i++;
                    Log.w("FileUtils", "Cell Value: " +  myCell.toString());
                    Toast.makeText(context, "cell Value: " + myCell.toString(), Toast.LENGTH_SHORT).show();
                }
                Student student=new Student(values[0],values[1],values[2],values[3],values[4],values[5],values[6]);
                arrayList.add(student);


            }
            MyTask myTask=new MyTask();
            myTask.execute();
        }catch (Exception e){
            e.printStackTrace();
        }

        return;
    }
    private static boolean isExternalStorageReadOnly() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED_READ_ONLY.equals(extStorageState)) {
            return true;
        }
        return false;
    }

    private static boolean isExternalStorageAvailable() {
        String extStorageState = Environment.getExternalStorageState();
        if (Environment.MEDIA_MOUNTED.equals(extStorageState)) {
            return true;
        }
        return false;
    }
    class MyTask extends AsyncTask<Void,Void,Void> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            Date date=new Date();
            SimpleDateFormat simpleDateFormat=new SimpleDateFormat("ddMMyyyy");
            String sdate=simpleDateFormat.format(date).toString().trim();
            databaseReference= FirebaseDatabase.getInstance().getReference().child("GGSIPU").child("student");

        }

        @Override
        protected void onPostExecute(Void aVoid) {
            super.onPostExecute(aVoid);
            int size=arrayList.size();
            for(int i=0;i<size;i++){
                arrayList.remove(arrayList.size()-1);
            }
        }

        @Override
        protected Void doInBackground(Void... voids) {
            for( int i=0;i<arrayList.size();i++){
                inputdata(arrayList.get(i));

            }

            return null;


        }
        private void inputdata(final Student student){

            databaseReference.child(student.getEnrollNo()).setValue(student).addOnSuccessListener(new OnSuccessListener<Void>() {
                @Override
                public void onSuccess(Void aVoid) {
                    Log.e("DATAENTRY","SUCCESS");
                }
            }).addOnFailureListener(new OnFailureListener() {
                @Override
                public void onFailure(@NonNull Exception e) {
                    Log.e("DATAENTRY","FAILED");
                }
            });

        }
    }
}
