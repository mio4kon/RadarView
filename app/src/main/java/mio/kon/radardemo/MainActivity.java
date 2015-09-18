package mio.kon.radardemo;

import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;

import mio.kon.radarview.RadarMap;
import mio.kon.radarview.RadarView;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        setContentView (R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById (R.id.toolbar);
        setSupportActionBar (toolbar);


        RadarView radarView = (RadarView) findViewById (R.id.radarView);
        RadarMap radarMap = new RadarMap ();
        radarMap.put ("综合",67.2f);
        radarMap.put ("KDA",68.2f);
        radarMap.put ("发育",70f);
        radarMap.put ("推进", 82f);
        radarMap.put ("生存", 50.5f);
        radarMap.put ("输出", 70f);
        radarView.setAbility (radarMap);
    }


}
