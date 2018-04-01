package com.exe.paradox;

import android.animation.Animator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.View;
import android.view.ViewAnimationUtils;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.AlphaAnimation;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;
import com.exe.paradox.api.model.Hints;
import com.exe.paradox.api.model.Profile;
import com.exe.paradox.api.response.AcknowedgementResponse;
import com.exe.paradox.api.response.HintResponse;
import com.exe.paradox.api.response.LevelResponse;
import com.exe.paradox.api.response.ReadOneResponse;
import com.exe.paradox.api.rest.ApiClient;
import com.exe.paradox.api.rest.ApiInterface;
import com.exe.paradox.util.Constants;
import com.shashank.sony.fancydialoglib.Animation;
import com.shashank.sony.fancydialoglib.FancyAlertDialog;
import com.shashank.sony.fancydialoglib.FancyAlertDialogListener;
import com.shashank.sony.fancydialoglib.Icon;
import com.squareup.picasso.Picasso;
import com.wajahatkarim3.easyflipview.EasyFlipView;

import java.util.ArrayList;
import java.util.Random;

import am.appwise.components.ni.NoInternetDialog;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class QuestionActivity extends AppCompatActivity {
    public int c = 0;
    public int hint_count = 1;
    public String hint1, hint2, hint3;
    public ImageView imageView;
    Button submitButton;
    EditText answer;
    public static String url;
    TextView hint_number, hint_text, hint_next, hint_prev, t3;
    NoInternetDialog noInternetDialog;

    RelativeLayout rootLayout;

    NestedScrollView parent;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_question);
        rootLayout = findViewById(R.id.qroot);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
            Window w = getWindow(); // in Activity's onCreate() for instance
            w.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS, WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
        }
        if (savedInstanceState == null) {
            rootLayout.setVisibility(View.INVISIBLE);

            ViewTreeObserver viewTreeObserver = rootLayout.getViewTreeObserver();
            if (viewTreeObserver.isAlive()) {
                viewTreeObserver.addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        circularRevealActivity();
                        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN) {
                            rootLayout.getViewTreeObserver().removeGlobalOnLayoutListener(this);
                        } else {
                            rootLayout.getViewTreeObserver().removeOnGlobalLayoutListener(this);

                        }
                    }
                });
            }
        }

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar_question);
        setSupportActionBar(toolbar);
        noInternetDialog = new NoInternetDialog.Builder(this).build();
        hint_number = findViewById(R.id.h);
        answer = findViewById(R.id.e4);
        t3 = findViewById(R.id.t3);
        hint_text = findViewById(R.id.h1);
        hint_next = findViewById(R.id.h2);
        hint_prev = findViewById(R.id.h3);
        hint_prev.setVisibility(View.INVISIBLE);
        imageView = findViewById(R.id.i1);
        final ProgressBar pBar = findViewById(R.id.pBar);
        pBar.getIndeterminateDrawable().setColorFilter(ContextCompat.getColor(this, R.color.blue), PorterDuff.Mode.SRC_IN);
        parent = findViewById(R.id.parent_question);
        submitButton = findViewById(R.id.submit);
        final EasyFlipView flip = findViewById(R.id.flip);
        final TextView hintText = findViewById(R.id.t2);

        final ApiInterface apiService =
                ApiClient.getClient().create(ApiInterface.class);

        final GPlusFragment gPlusFragment = new GPlusFragment();

        hintText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (c == 0) {
                    AlphaAnimation animObj = new AlphaAnimation(0, 1);
                    animObj.setDuration(1000);
                    hintText.startAnimation(animObj);
                    hintText.setText("Back to Question!!");
                    c = 1;
                } else {
                    AlphaAnimation animObj = new AlphaAnimation(0, 1);
                    animObj.setDuration(1000);
                    hintText.startAnimation(animObj);
                    hintText.setText("Go for Hints!!");
                    c = 0;
                }
                flip.flipTheView(true);
            }
        });

        final Call<ReadOneResponse> responseCall = apiService.getProfile(gPlusFragment.getSignId(), Constants.FETCH_TYPE, Constants.FETCH_TOKEN);

        submitButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                submitAnswer(responseCall, apiService, gPlusFragment);
            }
        });

        responseCall.enqueue(new Callback<ReadOneResponse>() {
            @Override
            public void onResponse(Call<ReadOneResponse> call, Response<ReadOneResponse> response) {
                if (response.isSuccessful()) {
                    if (response.body().getProfileData().size() > 0) {
                        Profile profile = response.body().getProfileData().get(0);
                        if (profile.getLevel().matches("7")) {
                            parent.setVisibility(View.GONE);
                            showCompleted();
                        } else {
                            Call<LevelResponse> levelResponseCall = apiService.getLevelResponse(Constants.FETCH_TOKEN, String.valueOf(Constants.FETCH_TYPE), profile.getLevel());
                            t3.setText("Level " + profile.getLevel());
                            levelResponseCall.enqueue(new Callback<LevelResponse>() {
                                @Override
                                public void onResponse(@NonNull Call<LevelResponse> call, @NonNull Response<LevelResponse> response) {
                                    if (response.body().getLevelUrlList().size() > 0) {
                                        url = "http://exe.nith.ac.in/paradox" + response.body().getLevelUrlList().get(0).getUrl();
                                        Picasso.get().load("http://exe.nith.ac.in/paradox" + response.body().getLevelUrlList().get(0).getUrl()).into(imageView, new com.squareup.picasso.Callback() {
                                            @Override
                                            public void onSuccess() {
                                                pBar.setVisibility(View.GONE);
                                            }

                                            @Override
                                            public void onError(Exception e) {

                                            }
                                        });
                                    } else {
                                        Toast.makeText(QuestionActivity.this, "No more questions available for now", Toast.LENGTH_SHORT).show();
                                        startActivity(new Intent(QuestionActivity.this, HomeActivity.class));
                                        finish();
                                    }
                                }

                                @Override
                                public void onFailure(Call<LevelResponse> call, Throwable t) {
                                    Toast.makeText(QuestionActivity.this, "ERROR", Toast.LENGTH_SHORT).show();
                                }
                            });
                        }
                    }
                }
            }

            @Override
            public void onFailure(Call<ReadOneResponse> call, Throwable t) {
                Log.d(getClass().getSimpleName(), "FAIL");
            }
        });

        responseCall.clone().enqueue(new Callback<ReadOneResponse>() {
            @Override
            public void onResponse(Call<ReadOneResponse> call, Response<ReadOneResponse> response) {
                //Fetching Hints
                Call<HintResponse> callHint = apiService.getHints(Integer.parseInt(response.body().getProfileData().get(0).getLevel()), Constants.FETCH_TOKEN, Constants.FETCH_TYPE);
                callHint.enqueue(new Callback<HintResponse>() {
                    @Override
                    public void onResponse(Call<HintResponse> call, Response<HintResponse> response) {
                        if (response.isSuccessful()) {
                            ArrayList<Hints> hints = response.body().getList();
                            if (hints.size() > 0) {
                                if (hints.get(0).getHint1() != null)
                                    hint1 = hints.get(0).getHint1();
                                AlphaAnimation animObj = new AlphaAnimation(0, 1);
                                animObj.setDuration(1000);
                                hint_text.startAnimation(animObj);
                                hint_text.setText(hint1);
                                if (hints.get(0).getHint2() != null)
                                    hint2 = hints.get(0).getHint2();
                                if (hints.get(0).getHint3() != null)
                                    hint3 = hints.get(0).getHint3();
                            }
                        }
                    }

                    @Override
                    public void onFailure(Call<HintResponse> call, Throwable t) {
                        Log.d(getClass().getSimpleName(), "ERROR");
                    }
                });
            }

            @Override
            public void onFailure(Call<ReadOneResponse> call, Throwable t) {

            }
        });


        AlphaAnimation animObj = new AlphaAnimation(0, 1);
        animObj.setDuration(1000);
        hint_number.startAnimation(animObj);
        hint_number.setText("HINT NO " + Integer.toString(hint_count));


        hint_next.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (hint_count) {
                    case 1:
                        hint_count = 2;
                        AlphaAnimation animObj = new AlphaAnimation(0, 1);
                        animObj.setDuration(1000);
                        hint_number.startAnimation(animObj);
                        hint_text.startAnimation(animObj);
                        hint_number.setText("HINT NO " + Integer.toString(hint_count));
                        hint_text.setText(hint2);
                        hint_prev.setVisibility(View.VISIBLE);
                        break;
                    case 2:
                        hint_count = 3;
                        AlphaAnimation animObj1 = new AlphaAnimation(0, 1);
                        animObj1.setDuration(1000);
                        hint_number.startAnimation(animObj1);
                        hint_text.startAnimation(animObj1);
                        hint_number.setText("HINT NO " + Integer.toString(hint_count));
                        hint_text.setText(hint3);
                        break;
                    default:
                        Toast.makeText(QuestionActivity.this, "No more hint left!!", Toast.LENGTH_SHORT).show();
                        break;
                }
            }
        });

        hint_prev.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                switch (hint_count) {
                    case 3:     //Toast.makeText(QuestionActivity.this, "One more hint left!!", Toast.LENGTH_SHORT).show();
                        hint_count = 2;
                        AlphaAnimation animObj = new AlphaAnimation(0, 1);
                        animObj.setDuration(1000);
                        hint_number.startAnimation(animObj);
                        hint_text.startAnimation(animObj);
                        hint_number.setText("HINT NO " + Integer.toString(hint_count));
                        hint_text.setText(hint2);
                        break;
                    case 2:     //Toast.makeText(QuestionActivity.this, "No more hint left!!", Toast.LENGTH_SHORT).show();

                        hint_count = 1;
                        AlphaAnimation animObj1 = new AlphaAnimation(0, 1);
                        animObj1.setDuration(1000);
                        hint_number.startAnimation(animObj1);
                        hint_text.startAnimation(animObj1);
                        hint_number.setText("HINT NO " + Integer.toString(hint_count));
                        hint_text.setText(hint1);
                        hint_prev.setVisibility(View.INVISIBLE);
                        break;
                    default:    //Toast.makeText(QuestionActivity.this, "No more hint left!!", Toast.LENGTH_SHORT).show();

                        break;

                }
            }
        });
    }

    public void animateIntent(View v) {
        if (url != null) {
            Intent intent = new Intent(QuestionActivity.this, ImageZoomActivity.class);
            String transitionName = getString(R.string.transition_string);
            View viewStart = findViewById(R.id.i1);
            ActivityOptionsCompat options =
                    ActivityOptionsCompat.makeSceneTransitionAnimation(QuestionActivity.this,
                            viewStart,
                            transitionName
                    );
            ActivityCompat.startActivity(QuestionActivity.this, intent, options.toBundle());
        }
    }

    private void submitAnswer(Call<ReadOneResponse> responseCall, final ApiInterface apiService, final GPlusFragment gPlusFragment) {
        if (validate()) {
            responseCall.clone().enqueue(new Callback<ReadOneResponse>() {
                @Override
                public void onResponse(Call<ReadOneResponse> call, Response<ReadOneResponse> response) {
                    Profile profile = response.body().getProfileData().get(0);
                    Call<AcknowedgementResponse> acknowledgementResponseCall = apiService.submitAnswer(Constants.FETCH_TOKEN, String.valueOf(Constants.FETCH_TYPE), profile.getLevel(), answer.getText().toString().toLowerCase(), gPlusFragment.getSignId());
                    acknowledgementResponseCall.enqueue(new Callback<AcknowedgementResponse>() {
                        @Override
                        public void onResponse(Call<AcknowedgementResponse> call, Response<AcknowedgementResponse> response) {
                            if (response.body().getMessage().matches("true")) {
                                showCorrect();
                            } else {
                                showFalse();
                            }
                        }

                        @Override
                        public void onFailure(Call<AcknowedgementResponse> call, Throwable t) {

                        }
                    });
                }

                @Override
                public void onFailure(Call<ReadOneResponse> call, Throwable t) {

                }
            });
        }
    }

    private boolean validate() {
        if (answer.getText() == null) {
            Toast.makeText(QuestionActivity.this, "Answer not entered", Toast.LENGTH_LONG).show();
            return false;
        } else if (answer.getText().toString().trim().length() == 0) {
            Toast.makeText(QuestionActivity.this, "Answer not entered", Toast.LENGTH_LONG).show();
            return false;
        }
        return true;
    }

    private void showCorrect() {
        new FancyAlertDialog.Builder(this)
                .setTitle("Correct Answer")
                .setBackgroundColor(Color.parseColor("#32CD32"))
                .setMessage(getMessage())
                .setPositiveBtnBackground(Color.parseColor("#32CD32"))
                .setPositiveBtnText("Rate")
                .setNegativeBtnText("Go back")
                .setAnimation(Animation.SIDE)
                .isCancellable(false)
                .setIcon(R.drawable.ic_alert_tick, Icon.Visible)
                .OnNegativeClicked(new FancyAlertDialogListener() {
                    @Override
                    public void OnClick() {
                        startActivity(new Intent(QuestionActivity.this, HomeActivity.class));
                        finish();
                    }
                })
                .OnPositiveClicked(new FancyAlertDialogListener() {
                    @Override
                    public void OnClick() {
                        //Link to the Play store account.
                    }
                })
                .build();
    }

    private void showCompleted() {
        new FancyAlertDialog.Builder(this)
                .setTitle("Completed")
                .setBackgroundColor(Color.parseColor("#32CD32"))
                .setMessage("")
                .setPositiveBtnBackground(Color.parseColor("#32CD32"))
                .setPositiveBtnText("Rate")
                .setNegativeBtnText("Go back")
                .setAnimation(Animation.SIDE)
                .isCancellable(false)
                .setIcon(R.drawable.ic_alert_tick, Icon.Visible)
                .OnNegativeClicked(new FancyAlertDialogListener() {
                    @Override
                    public void OnClick() {
                        startActivity(new Intent(QuestionActivity.this, HomeActivity.class));
                        finish();
                    }
                })
                .OnPositiveClicked(new FancyAlertDialogListener() {
                    @Override
                    public void OnClick() {
                        //Link to the Play store account.
                    }
                })
                .build();
    }

    private void showFalse() {
        new FancyAlertDialog.Builder(this)
                .setTitle("Wrong Answer")
                .setBackgroundColor(Color.parseColor("#B22222"))
                .setMessage(getMessage())
                .setPositiveBtnBackground(Color.parseColor("#B22222"))
                .setPositiveBtnText("Rate")
                .setNegativeBtnText("Go back")
                .setAnimation(Animation.SIDE)
                .isCancellable(true)
                .setIcon(R.drawable.wrong, Icon.Visible)
                .OnNegativeClicked(new FancyAlertDialogListener() {
                    @Override
                    public void OnClick() {
                        startActivity(new Intent(QuestionActivity.this, QuestionActivity.class));
                        finish();
                    }
                })
                .OnPositiveClicked(new FancyAlertDialogListener() {
                    @Override
                    public void OnClick() {
                        //Link to the Play store account.
                    }
                })
                .build();
    }

    private String getMessage() {
        String array[] = getResources().getStringArray(R.array.message_array);
        return array[new Random().nextInt(array.length)];
    }

    private void circularRevealActivity() {

        int cx = rootLayout.getWidth() / 2;
        int cy = rootLayout.getHeight() / 2;

        float finalRadius = Math.max(rootLayout.getWidth(), rootLayout.getHeight());

        // create the animator for this view (the start radius is zero)
        Animator circularReveal = ViewAnimationUtils.createCircularReveal(rootLayout, cx, 0, 0, finalRadius * 2);
        circularReveal.setDuration(1000);

        // make the view visible and start the animation
        rootLayout.setVisibility(View.VISIBLE);
        circularReveal.start();
    }

    private void backCircular() {

        int cx = rootLayout.getWidth() / 2;
        int cy = rootLayout.getHeight() / 2;

        float finalRadius = Math.max(rootLayout.getWidth(), rootLayout.getHeight());

        // create the animator for this view (the start radius is zero)
        Animator circularReveal = ViewAnimationUtils.createCircularReveal(rootLayout, cx, 0, finalRadius * 2, 0);
        circularReveal.setDuration(1000);

        // make the view visible and start the animation
        circularReveal.start();
        circularReveal.addListener(new Animator.AnimatorListener() {
            @Override
            public void onAnimationStart(Animator animation) {

            }

            @Override
            public void onAnimationEnd(Animator animation) {
                rootLayout.setVisibility(View.INVISIBLE);

                finish();
            }

            @Override
            public void onAnimationCancel(Animator animation) {

            }

            @Override
            public void onAnimationRepeat(Animator animation) {

            }
        });
    }


    @Override
    public void onBackPressed() {
        backCircular();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        noInternetDialog.onDestroy();
    }
}

