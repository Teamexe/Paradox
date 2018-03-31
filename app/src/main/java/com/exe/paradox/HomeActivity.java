package com.exe.paradox;

import android.animation.Animator;
import android.content.Intent;
import android.graphics.Color;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.PagerAdapter;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.exe.paradox.adapter.FeaturedAdapter;
import com.exe.paradox.adapter.ProjectAdapter;
import com.exe.paradox.util.Preferences;
import com.exe.paradox.util.RecyclerItemClickListener;
import com.github.florent37.parallax.ScrollView;
import com.github.javiersantos.materialstyleddialogs.MaterialStyledDialog;
import com.mikepenz.iconics.IconicsDrawable;
import com.mikepenz.material_design_iconic_typeface_library.MaterialDesignIconic;
import com.pixelcan.inkpageindicator.InkPageIndicator;
import com.squareup.picasso.Picasso;

import de.hdodenhof.circleimageview.CircleImageView;

public class HomeActivity extends AppCompatActivity {

    CircleImageView img;
    TextView name;
    LinearLayout rankings, paradox, stats, referral, members;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_home);

        ScrollView rootLayout = (ScrollView) findViewById(R.id.holder);
        if (savedInstanceState == null) {
            rootLayout.setVisibility(View.INVISIBLE);

            ViewTreeObserver viewTreeObserver = rootLayout.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        ScrollView rootLayout = (ScrollView) findViewById(R.id.holder);
                        enterReveal();

                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            rootLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);
                        }
                    }
                });
            }
        }

        GPlusFragment beta = new GPlusFragment();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }

        ViewPager pager = findViewById(R.id.pager);
        PagerAdapter pagerAdapter = new ScreenSlidePagerAdapter(getSupportFragmentManager());
        pager.setAdapter(pagerAdapter);
        pager.setClipToPadding(false);
        pager.setPadding(60, 0, 60, 0);
        pager.setPageMargin(20);
        InkPageIndicator inkPageIndicator = findViewById(R.id.indicator);
        inkPageIndicator.setViewPager(pager);

        name = findViewById(R.id.title_name);
        img = findViewById(R.id.acc_img);

        if (beta.getImg() != "null")
            Picasso.get().load(beta.getImg()).placeholder(R.drawable.user_icon).into(img);
        name.setText(beta.getDisplayName());
        setAllListeners();

        RecyclerView recv = findViewById(R.id.recv_orange);
        ProjectAdapter projectAdapter = new ProjectAdapter(this);
        recv.setLayoutManager(new GridLayoutManager(HomeActivity.this, 3) {
            @Override
            public boolean canScrollVertically() {
                return false;
            }
        });
        recv.setAdapter(projectAdapter);
        final String[] titlesExe = {"One", "Two", "Three", "Four", "Five", "Six", "Seven", "Eight", "Nine"};
        final String[] descExe = {"werqwer", "sdewafdva", "ewafsfgagEFDDAF", "SFASRF3EFASFDFAAFS", "fsdfwfsfafwef", "qdqdsadqwdadad", "sadfasdfwaefcQCD", "FADSFFCDCasdvafdsaf", "ufdhsadflgsajf"};
        final String[] linkExe = {"https://github.com/octacode", "https://github.com/octacode", "https://github.com/octacode", "https://github.com/octacode", "https://github.com/octacode", "https://github.com/octacode", "https://github.com/octacode", "https://github.com/octacode", "https://github.com/octacode"};
        recv.addOnItemTouchListener(new RecyclerItemClickListener(HomeActivity.this, new RecyclerItemClickListener.OnItemClickListener() {
            @Override
            public void onItemClick(View view, final int position) {
                final MaterialStyledDialog.Builder dialog = new MaterialStyledDialog.Builder(HomeActivity.this)
                        .setHeaderDrawable(R.drawable.header)
                        .setIcon(new IconicsDrawable(HomeActivity.this).icon(MaterialDesignIconic.Icon.gmi_github).color(Color.WHITE))
                        .withDialogAnimation(true)
                        .setTitle(titlesExe[position])
                        .setDescription(descExe[position])
                        .setPositiveText("GitHub")
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(linkExe[position])));
                            }
                        })
                        .setNegativeText("Not now");
                dialog.build().show();
            }
        }));

        RecyclerView featuredProjects = findViewById(R.id.featured_projects);
        FeaturedAdapter featuredAdapter = new FeaturedAdapter(this);
        featuredProjects.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false));
        featuredProjects.setAdapter(featuredAdapter);
    }

    private void setAllListeners() {
        //rankings, paradox, stats, referral, members
        setButtons();
        rankings.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, LeaderboardActivity.class));
            }
        });

        paradox.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, QuestionActivity.class));
            }
        });

        stats.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, StatsActivity.class));
            }
        });

        if (!Preferences.getRef(this)) {
            referral.setVisibility(View.GONE);
        }

        referral.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, ReferActivity.class));
            }
        });

        members.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(HomeActivity.this, MembersActivity.class));
            }
        });
    }

    private void setButtons() {
        rankings = findViewById(R.id.rankings_ll);
        paradox = findViewById(R.id.paradox_ll);
        stats = findViewById(R.id.stats_ll);
        referral = findViewById(R.id.referral_ll);
        members = findViewById(R.id.members_ll);
    }

    private void enterReveal() {
        ScrollView rootLayout = (ScrollView) findViewById(R.id.holder);
        int cx = rootLayout.getWidth() / 2;
        int cy = rootLayout.getHeight() / 2;

        float finalRadius = Math.max(rootLayout.getWidth(), rootLayout.getHeight());

        // create the animator for this view (the start radius is zero)
        Animator circularReveal = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.LOLLIPOP) {
            circularReveal = ViewAnimationUtils.createCircularReveal(rootLayout, cx, 0, 0, finalRadius);
        }
        circularReveal.setDuration(1600);

        // make the view visible and start the animation
        rootLayout.setVisibility(View.VISIBLE);
        circularReveal.start();
    }

    private class ScreenSlidePagerAdapter extends FragmentStatePagerAdapter {

        int[] drawables = {R.drawable.hu7, R.drawable.hu1, R.drawable.hu2, R.drawable.hu3, R.drawable.hu4, R.drawable.hu5, R.drawable.hu6};

        ScreenSlidePagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getItem(int position) {
            return new MyFragment(drawables[position], position);
        }

        @Override
        public int getCount() {
            return drawables.length;
        }
    }

}



