package com.example.myapplication;

import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.PointF;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.View.OnTouchListener;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements OnClickListener,
        OnTouchListener {

    ImageView img1, img2;
    ImageButton button1, button2;
    Button button;

    //upload images and draw lines
    Bitmap bmp, bmp2;
    Bitmap alteredBitmap, alteredBitmap2;
    Canvas canvas, canvas2;
    Paint paint;
    Matrix matrix, matrix2;
    float downx = 0;
    float downy = 0;
    float upx = 0;
    float upy = 0;

    //img1+bmp + canvas
    ArrayList<Line> desList = new ArrayList<Line>();
    //img2+bmp2+canvas2
    ArrayList<Line> srcList = new ArrayList<Line>();

    //change mode to edit
    boolean isDrawMode = true;

    // move lines
    float movex, movey;
    Line tempLine = null;
    Point startPoint, endPoint;
    boolean src = false, des = false;

    //morphing
    TextView frameNumber, frameProgress;
    int frameNum;
    Button morp;
    SeekBar bar;
    ArrayList<Frame> frames = new ArrayList<Frame>();
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        //set landscape screen
        setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);

        // get IDs
        img1 = this.findViewById(R.id.imgView1);
        img2 = this.findViewById(R.id.imgView2);
        button1 = this.findViewById(R.id.button1);
        button2 = this.findViewById(R.id.button2);
        button = this.findViewById(R.id.button);
        frameNumber = this.findViewById(R.id.frameNum);
        frameProgress = this.findViewById(R.id.frameProgress);
        morp = this.findViewById(R.id.morp);
        bar = this.findViewById(R.id.seekBar);

        //activities
        button1.setOnClickListener(this);
        button2.setOnClickListener(this);
        img1.setOnTouchListener(this);
        button.setOnClickListener(new View.OnClickListener(){

            @Override
            public void onClick(View v) {
                isDrawMode = !isDrawMode;

            }
        });

        morp.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View view)
            {
                ArrayList<Line> tempL = new ArrayList<>();
                ArrayList<Line> tempR = new ArrayList<>();
                Line ll = new Line(1, 0, 1, 1);
                Line lr = new Line(1, 0, 1, 1);
                tempL.add(ll);
                tempR.add(lr);
                frameNum = Integer.parseInt(frameNumber.getText().toString());
                frames.clear();
                //Toast.makeText(getApplicationContext(), frameNumber.getText().toString(), Toast.LENGTH_SHORT).show();
                for(int i = 0; i <= frameNum; i++) {
                    Frame f = new Frame(bmp2, bmp, srcList, desList, (double) i / frameNum);
//                    Frame f = new Frame(bmp2, bmp, tempL, tempR, (double) i / frameNum);
                    frames.add(f);

                }
                bar.setMax(frameNum);

            }
        });


        //bar.setMax(frameNum);
        bar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            int prog = 0;
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                prog = progress;
                frameProgress.setText("Frame Progress: " + progress + "/" + bar.getMax());
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {

            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                frameProgress.setText("Frame Progress: " + prog + "/" + bar.getMax());
                bmp = frames.get(prog).getBitmap();
                canvas.drawBitmap(bmp, 0, 0, paint);
                if(prog == 0 || prog == bar.getMax()) {
                    desList = frames.get(prog).lineList;
                    for (Line c : desList) {
                        c.onDrawDes();
                    }
                }
                else {
                    desList = null;
                }


            }
        });
    }

    //buttons activities
    public void onClick(View v) {
        Intent choosePictureIntent = new Intent(
                Intent.ACTION_PICK,
                android.provider.MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
     if (v == button1) {
            startActivityForResult(choosePictureIntent, 0);
        } else if(v == button2){
            startActivityForResult(choosePictureIntent, 1);
        }

    }

    protected void onActivityResult(int requestCode, int resultCode,
                                    Intent intent) {
        super.onActivityResult(requestCode, resultCode, intent);

        // set image in image view 1
        if (resultCode == RESULT_OK && requestCode == 0) {
            Uri imageFileUri = intent.getData();
            try {
                BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();

                bmpFactoryOptions.inJustDecodeBounds = false;
                bmp = BitmapFactory.decodeStream(getContentResolver().openInputStream(
                        imageFileUri), null, bmpFactoryOptions);

                bmp = Bitmap.createScaledBitmap(bmp, img1.getWidth(), img1.getHeight(), false);

                alteredBitmap = Bitmap.createBitmap(img1.getWidth(), img1
                        .getHeight(), bmp.getConfig());

                canvas = new Canvas(alteredBitmap);
                initPaint();
                matrix = new Matrix();
                canvas.drawBitmap(bmp, matrix, paint);

                img1.setImageBitmap(alteredBitmap);


            } catch (Exception e) {
                Log.v("ERROR", e.toString());
            }

        // set image in image view
        }else if(resultCode == RESULT_OK && requestCode == 1){
            Uri imageFileUri = intent.getData();
            try {
                BitmapFactory.Options bmpFactoryOptions = new BitmapFactory.Options();

                bmpFactoryOptions.inJustDecodeBounds = false;
                bmp2 = BitmapFactory.decodeStream(getContentResolver().openInputStream(
                        imageFileUri), null, bmpFactoryOptions);

                bmp2 = Bitmap.createScaledBitmap(bmp2, img2.getWidth(), img2.getHeight(), false);

                alteredBitmap2 = Bitmap.createBitmap(img2.getWidth(), img2
                        .getHeight(), bmp2.getConfig());

                canvas2 = new Canvas(alteredBitmap2);
                initPaint();

                matrix2 = new Matrix();
                canvas2.drawBitmap(bmp2, matrix2, paint);

                img2.setImageBitmap(alteredBitmap2);
            } catch (Exception e) {
                Log.v("ERROR", e.toString());
            }
        }
        img1.setOnTouchListener(this);
        img2.setOnTouchListener(this);

    }


    //finger draw
    public boolean onTouch(View v, MotionEvent event) {
        int action = event.getAction();

        if(desList == null){
            return true;
        }

        switch (action) {

            case MotionEvent.ACTION_DOWN:
                startPoint = null;
                endPoint = null;

                // View mode
                if(isDrawMode) {
                    //get start (x,y)
                    downx = event.getX();
                    downy = event.getY();
                }

                // Edit mode
                else {
                    movex = event.getX();
                    movey = event.getY();
                    if (v == img2) {
                        for (Line c : srcList) {
                            //change start point
                            if ((movex >= (c.start.x - 20)) && (movex <= (c.start.x + 20))
                                    && (movey >= (c.start.y - 20)) && (movey <= (c.start.y + 20))) {
                                tempLine = c;
                                startPoint = c.start;
                                return true;
                            }
                            // change end point
                            else if ((movex >= (c.end.x - 20)) && (movex <= (c.end.x + 20))
                                    && (movey >= (c.end.y - 20)) && (movey <= (c.end.y + 20))) {
                                tempLine = c;
                                endPoint = c.end;
                                return true;
                            }
                        }
                    } else if (v == img1) {
                        for (Line c : desList) {
                            //change start point
                            if ((movex >= (c.start.x - 20)) && (movex <= (c.start.x + 20))
                                    && (movey >= (c.start.y - 20)) && (movey <= (c.start.y + 20))) {
                                tempLine = c;
                                startPoint = c.start;
                                return true;
                            }
                            // change end point
                            else if ((movex >= (c.end.x - 20)) && (movex <= (c.end.x + 20))
                                    && (movey >= (c.end.y - 20)) && (movey <= (c.end.y + 20))) {
                                tempLine = c;
                                endPoint = c.end;
                                return true;
                            }
                        }

                    }
                }
                break;
            case MotionEvent.ACTION_MOVE:

                //pos down
                upx = event.getX();
                upy = event.getY();

                // View mode
                if(isDrawMode) {
                    canvas.drawBitmap(bmp, 0, 0, paint);
                    canvas2.drawBitmap(bmp2, 0, 0, paint);

                    for (Line c : srcList) {
                        c.onDrawSrc();
                    }
                    for (Line c : desList) {
                        c.onDrawDes();
                    }
                    canvas.drawLine(downx, downy, upx, upy, paint);
                    canvas2.drawLine(downx, downy, upx, upy, paint);
                    img1.invalidate();
                    img2.invalidate();
                }

                // Edit mode
                else{
                    canvas.drawBitmap(bmp, 0, 0, paint);
                    canvas2.drawBitmap(bmp2, 0, 0, paint);

                    //if move start point
                    if(startPoint != null) {
                        tempLine.start.x = (int)upx;
                        tempLine.start.y = (int)upy;

                        for (Line c : srcList) {
                            c.onDrawSrc();
                        }
                        for (Line c : desList) {
                            c.onDrawDes();
                        }
                    }

                    // if move end point
                    else if(endPoint != null){

                        tempLine.end.x = (int)upx;
                        tempLine.end.y = (int)upy;

                        for (Line c : srcList) {
                            c.onDrawSrc();
                        }
                        for (Line c : desList) {
                            c.onDrawDes();
                        }
                    } else{
                        for (Line c : srcList) {
                            c.onDrawSrc();
                        }
                        for (Line c : desList) {
                            c.onDrawDes();
                        }
                    }


                    // image views redraw
                    img1.invalidate();
                    img2.invalidate();
                }
                break;
            case MotionEvent.ACTION_UP:
                // View mode
                upx = event.getX();
                upy = event.getY();
                if(isDrawMode) {

                    // add line to array of srcList
                    Line l1 = new Line(downx, downy, upx, upy);
                    srcList.add(l1);
                    Line l2 = new Line(downx, downy, upx, upy);
                    desList.add(l2);

                    //draw line & bitmap
                    canvas.drawBitmap(bmp, 0, 0, paint);
                    canvas2.drawBitmap(bmp2, 0, 0, paint);

                    for (Line c : srcList) {
                        c.onDrawSrc();
                    }
                    for (Line c : desList) {
                        c.onDrawDes();
                    }

                    img1.invalidate();
                    img2.invalidate();
                }

                // Edit mode
                else{
                    canvas.drawBitmap(bmp, 0, 0, paint);
                    canvas2.drawBitmap(bmp2, 0, 0, paint);

                    //if move start point
                    if(startPoint != null) {
                        tempLine.start.x = (int)upx;
                        tempLine.start.y = (int)upy;

                        for (Line c : srcList) {
                            c.onDrawSrc();
                        }
                        for (Line c : desList) {
                            c.onDrawDes();
                        }
                    }

                    // if move end point
                    else if(endPoint != null){

                        tempLine.end.x = (int)upx;
                        tempLine.end.y = (int)upy;

                        for (Line c : srcList) {
                            c.onDrawSrc();
                        }
                        for (Line c : desList) {
                            c.onDrawDes();
                        }
                    } else{
                        for (Line c : srcList) {
                            c.onDrawSrc();
                        }
                        for (Line c : desList) {
                            c.onDrawDes();
                        }
                    }



                    // image views redraw
                    img1.invalidate();
                    img2.invalidate();

                }

                break;
            case MotionEvent.ACTION_CANCEL:
                break;
            default:
                break;
        }
        return true;
    }

    //paint settings
    protected void initPaint(){
        paint = new Paint();
        paint.setAntiAlias(true);
        paint.setDither(true);
        paint.setColor(Color.RED);
        paint.setStyle(Paint.Style.STROKE);
        paint.setStrokeJoin(Paint.Join.ROUND);
        paint.setStrokeCap(Paint.Cap.ROUND);
        paint.setStrokeWidth(15);


    }

    class Line{
        Point start;
        Point end;
        Point v, n;
        float length;
        float d, fp;


        public Line(float downx, float downy, float upx, float upy) {
            start = new Point((int) downx, (int) downy);
            end = new Point((int) upx, (int) upy);
        }


        protected void onDrawSrc(){

            canvas2.drawLine(start.x, start.y, end.x, end.y, paint);

        }


        protected void onDrawDes(){
            canvas.drawLine(start.x, start.y, end.x, end.y, paint);
        }

        protected void vector(){
            v = new Point(end.x - start.x, end.y - start.y);
            n = new Point(-v.y, v.x);
        }

        protected Point vector(Point s, Point e){

            Point p = new Point(e.x- s.x, e.y - s.y);
            return p;
        }



        public void length(){
            length = (float) Math.sqrt(Math.pow(v.x,2) + Math.pow(v.y, 2));
        }

        public float distance(Point p){
            Point vector = vector(p, start);
            vector();
            length();
            d = (vector.x * n.x + vector.y * n.y) / length ;
            return d;
        }

        public float fraction(Point p){
            Point vector = vector(start, p);
            vector();
            length();
            fp = (vector.x * v.x + vector.y * v.y)/(length*length);
            return fp;
        }
    }



    /*Morphing*/
    class Frame{
        Bitmap bitmap;
        Bitmap srcBitmap;
        Bitmap desBitmap;
        ArrayList<Line> lineList = new ArrayList<Line>();
        ArrayList<Line> srcList, desList;
        float fp[], d[], w[];
        float totalWeight = 0;
        Point srcPoints[], desPoints[], finalDesPoint, finalSrcPoint;
        PointF srcDelta,desDelta;
        final float a = (float) 0.01;
        final int b = 2;

        public Frame(Bitmap src, Bitmap des, ArrayList<Line> srcList, ArrayList<Line> desList, double t){
            fp = new float[srcList.size()];
            d = new float[srcList.size()];
            w = new float[srcList.size()];
            srcPoints = new Point[srcList.size()];
            desPoints = new Point[srcList.size()];
            srcBitmap = src;
            desBitmap = des;
            this.srcList = srcList;
            this.desList = desList;

            bitmap = Bitmap.createBitmap(img1.getWidth(), img1.getHeight(), src.getConfig());
            createLine(srcList, desList, t);
            average(t);

        }

        public Bitmap getBitmap(){
            return bitmap;
        }

        protected void createLine(ArrayList<Line> srcList, ArrayList<Line> desList, double t){
            for(int i = 0; i < srcList.size(); i++){
                float startx = (float) (srcList.get(i).start.x + t * (desList.get(i).start.x - srcList.get(i).start.x));
                float starty = (float) (srcList.get(i).start.y + t * (desList.get(i).start.y - srcList.get(i).start.y));
                float endy = (float) (srcList.get(i).end.y + t * (desList.get(i).end.y - srcList.get(i).end.y));
                float endx = (float) (srcList.get(i).end.x + t * (desList.get(i).end.x - srcList.get(i).end.x));

                Line l = new Line(startx, starty, endx, endy);
                lineList.add(l);
            }
        }

        //calc fraction of each line
        protected void calcFraction(Point p) {
            for (int i = 0; i < fp.length; i++) {
                fp[i] = lineList.get(i).fraction(p);
            }
        }

        //calc distance of each line
        protected void calcDistance(Point p) {
            for (int i = 0; i < lineList.size(); i++) {
                d[i] = lineList.get(i).distance(p);
            }
        }

        //calc prime points using distance and fraction
        protected void primePoint(Point p){
            int index = 0;
            for(Line l : srcList){
                l.vector();
                l.fraction(p);
                float x = l.start.x + fp[index] * l.v.x - d[index]*l.n.x/l.length;
                float y = l.start.y + fp[index] * l.v.y - d[index]*l.n.y/l.length;
                Point point = new Point((int)x - p.x, (int)y - p.y);
                srcPoints[index] = point;
                index++;

            }
            index = 0;
            for(Line l : desList){
                l.vector();
                l.fraction(p);
                float x = l.start.x + fp[index] * l.v.x - d[index]*l.n.x/l.length;
                float y = l.start.y + fp[index] * l.v.y - d[index]*l.n.y/l.length;
                Point point = new Point((int)x - p.x, (int)y - p.y);
                desPoints[index] = point;
                index++;

            }
        }

        //calc total weight
        protected void calcWeight(Point p){
            int index = 0;
            for(Line c : lineList){
                float tmp;
                if(c.fp < 0){
                    Point temp = c.vector(p, c.start);
                    tmp = (float) Math.sqrt(Math.pow(temp.x,2) + Math.pow(temp.y,2));

                } if (c.fp >1 ){
                    Point temp = c.vector(p, c.end);
                    tmp = (float) Math.sqrt(Math.pow(temp.x,2) + Math.pow(temp.y,2));

                } else{
                    tmp = d[index];
                }
                w[index++] = (float) Math.pow(1/(a + Math.abs(tmp)),b);
            }

        }

        protected void calcTotalWeight(Point p){
            totalWeight = 0;
            for(int i = 0; i < w.length; i++){
                totalWeight += w[i];
            }
        }

        //calc total points
        protected void calcTotalDelta(Point p){
            primePoint(p);
            srcDelta = new PointF(0,0);
            for(int i = 0; i < lineList.size(); i++){
                srcDelta.x += srcPoints[i].x * w[i];
                srcDelta.y += srcPoints[i].y * w[i];
            }
            desDelta = new PointF(0, 0);
            for(int i = 0; i < lineList.size(); i++){
                desDelta.x += desPoints[i].x * w[i];
                desDelta.y += desPoints[i].y * w[i];
            }
        }

        protected void calcAvgDelta(){
            finalSrcPoint = new Point ((int) (srcDelta.x / totalWeight),(int) (srcDelta.y / totalWeight));

            finalDesPoint = new Point ((int) (desDelta.x / totalWeight), (int) (desDelta.y / totalWeight));

        }
        protected void average(double t){
            for (int y = 0; y < bitmap.getHeight(); ++y) {
                for(int x = 0; x < bitmap.getWidth(); ++x) {
                    Point p = new Point(x, y);

                    calcFraction(p);
                    calcDistance(p);
                    calcWeight(p);

                    calcTotalWeight(p);
                    calcTotalDelta(p);
                    calcAvgDelta();

                    //src
                    int tempX = 0, tempY = 0;
                    tempX = p.x + finalSrcPoint.x;
                    tempY = p.y + finalSrcPoint.y;

                    if(tempX < 0){
                        tempX = 0;
                    } else if(tempX >= bitmap.getWidth()){
                        tempX = bitmap.getWidth() - 1;
                    }

                    if(tempY < 0){
                        tempY = 0;
                    } else if(tempY >= bitmap.getHeight()) {
                        tempY = bitmap.getHeight() - 1;
                    }
                    int srcClr = srcBitmap.getPixel(tempX, tempY);
                    int rSrc = (srcClr >> 16) & 0xFF;
                    int gSrc = (srcClr >> 8) & 0xFF;
                    int bSrc = srcClr & 0xFF;


                    //des
                    tempX = 0; tempY = 0;
                    tempX = p.x + finalDesPoint.x;
                    tempY = p.y + finalDesPoint.y;

                    if(tempX < 0){
                        tempX = 0;
                    } else if(tempX >= bitmap.getWidth()){
                        tempX = bitmap.getWidth() - 1;
                    }

                    if(tempY < 0){
                        tempY = 0;
                    } else if(tempY >= bitmap.getHeight()) {
                        tempY = bitmap.getHeight() - 1;
                    }

                    int desClr = desBitmap.getPixel(tempX, tempY);
                    int rDes = (desClr >> 16) & 0xFF;
                    int gDes = (desClr >> 8) & 0xFF;
                    int bDes = desClr & 0xFF;

                    int r =(int) (rSrc + t* (rDes - rSrc));
                    int g =(int) (gSrc + t* (gDes - gSrc));
                    int b =(int) (bSrc + t* (bDes - bSrc));

                    int clr = (0xff << 24) + (r << 16) + (g << 8) + b;
                    bitmap.setPixel(x, y, clr);
                }
            }

        }

    }
}