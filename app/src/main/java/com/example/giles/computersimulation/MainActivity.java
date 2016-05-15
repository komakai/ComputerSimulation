package com.example.giles.computersimulation;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Button;
import android.widget.ScrollView;
import android.widget.TextView;

import static com.example.giles.computersimulation.Instruction.*;

public class MainActivity extends AppCompatActivity implements ComputerSimulatorOutputListener {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        outputWindow = (TextView) findViewById(R.id.output);
        outputScrollWindow = (ScrollView) findViewById(R.id.scrolloutput);
        debugOutputWindow = (TextView) findViewById(R.id.debugoutput);
        debugOutputScrollWindow = (ScrollView) findViewById(R.id.scrolldebugoutput);
        ((Button)findViewById(R.id.runButton)).setOnClickListener(onClickListener);
        ((Button)findViewById(R.id.runDebugButton)).setOnClickListener(onClickListener);
    }

    public void appendOutput(String output) {
        outputWindow.append(output);
        outputWindow.append("\n");
        outputScrollWindow.fullScroll(View.FOCUS_DOWN);
    }

    public void appendDebugOutput(String debugOutput) {
        debugOutputWindow.append(debugOutput);
        debugOutputWindow.append("\n");
        debugOutputScrollWindow.fullScroll(View.FOCUS_DOWN);
    }

    @Override
    public void onOutput(String output) {
        appendOutput(output);
    }

    @Override
    public void onDebugOutput(String debugOutput) {
        appendDebugOutput(debugOutput);
    }

    public void doExec(boolean verbose) {
        ComputerSimulator computerSimulator = new ComputerSimulator( 100 );
        try {
            computerSimulator.setVerbose(verbose);
            computerSimulator.addOutputListener(MainActivity.this);
            computerSimulator.setAddress(PRINT_TENTEN_BEGIN).insertInstruction(MULT).insertInstruction(PRINT).insertInstruction(RET);
            computerSimulator.setAddress(MAIN_BEGIN).insertInstruction(PUSH, 1009).insertInstruction(PRINT);
            computerSimulator.insertInstruction(PUSH, 6);
            computerSimulator.insertInstruction(PUSH, 101).insertInstruction(PUSH, 10).insertInstruction(CALL, PRINT_TENTEN_BEGIN);
            computerSimulator.insertInstruction(STOP);
            computerSimulator.setAddress(MAIN_BEGIN).execute();
        } catch (ComputerSimulatorException computerSimulatorException) {
            onDebugOutput( computerSimulatorException.getMessage() );
        }
    }

    private static final int PRINT_TENTEN_BEGIN = 50;
    private static final int MAIN_BEGIN = 0;

    private View.OnClickListener onClickListener = new View.OnClickListener() {

        @Override
        public void onClick(final View v) {
            switch ( v.getId() ){
                case R.id.runButton:
                    doExec(false);
                    break;
                case R.id.runDebugButton:
                    doExec(true);
                    break;
            }
        }
    };

    private TextView outputWindow;
    private ScrollView outputScrollWindow;
    private TextView debugOutputWindow;
    private ScrollView debugOutputScrollWindow;
}
