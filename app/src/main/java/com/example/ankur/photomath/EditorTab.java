package com.example.ankur.photomath;


import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.ToggleButton;

import java.text.DecimalFormat;
import java.util.Stack;

/**
 * Created by ankur on 10/3/17.
 */

public class EditorTab extends Fragment {

    Button btnZero, btnOne, btnTwo, btnThree, btnFour, btnFive, btnSix, btnSeven, btnEight, btnNine,
            btnPlus, btnMinus, btnDivide, btnMultiply, btnLeftBrace, btnRightBrace, btnExp, btnDot, btnEqual, btnDel;
    TextView  tvAnswer;
    EditText tvEquation;
    ImageButton btnSteps;boolean c=true;
    static TextView tvSteps;

    static String ds = "";

    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        final View rootView = inflater.inflate(R.layout.editor_tab, container, false);


        btnZero = (Button) rootView.findViewById(R.id.btnZero);
        btnOne = (Button) rootView.findViewById(R.id.btnOne);
        btnTwo = (Button) rootView.findViewById(R.id.btnTwo);
        btnThree = (Button) rootView.findViewById(R.id.btnThree);
        btnFour = (Button) rootView.findViewById(R.id.btnFour);
        btnFive = (Button) rootView.findViewById(R.id.btnFive);
        btnSix = (Button) rootView.findViewById(R.id.btnSix);
        btnSeven = (Button) rootView.findViewById(R.id.btnSeven);
        btnEight = (Button) rootView.findViewById(R.id.btnEight);
        btnNine = (Button) rootView.findViewById(R.id.btnNine);
        btnPlus = (Button) rootView.findViewById(R.id.btnPlus);
        btnMinus = (Button) rootView.findViewById(R.id.btnMinus);
        btnDivide = (Button) rootView.findViewById(R.id.btnDivide);
        btnMultiply = (Button) rootView.findViewById(R.id.btnMultiply);
        btnLeftBrace = (Button) rootView.findViewById(R.id.btnLeftBrace);
        btnRightBrace = (Button) rootView.findViewById(R.id.btnRightBrace);
        btnExp = (Button) rootView.findViewById(R.id.btnExp);
        btnDot = (Button) rootView.findViewById(R.id.btnDot);
        btnEqual = (Button) rootView.findViewById(R.id.btnEqual);
        btnDel = (Button) rootView.findViewById(R.id.btnDel);

        btnSteps = (ImageButton) rootView.findViewById(R.id.btnSteps);
        tvSteps = (TextView) rootView.findViewById(R.id.tvSteps);

        tvEquation = (EditText) rootView.findViewById(R.id.tvEquation);
        tvAnswer = (TextView) rootView.findViewById(R.id.tvAnswer);

        btnZero.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvEquation.append("0");
            }
        });
        btnOne.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvEquation.append("1");
            }
        });

        btnTwo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvEquation.append("2");
            }
        });

        btnThree.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvEquation.append("3");
            }
        });

        btnFour.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvEquation.append("4");
            }
        });

        btnFive.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvEquation.append("5");
            }
        });

        btnSix.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvEquation.append("6");
            }
        });

        btnSeven.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvEquation.append("7");
            }
        });

        btnEight.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvEquation.append("8");
            }
        });

        btnNine.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvEquation.append("9");
            }
        });

        btnPlus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvEquation.append("+");
            }
        });

        btnMinus.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvEquation.append("-");
            }
        });

        btnDivide.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //tvEquation.append("÷");
                tvEquation.append("/");
            }
        });

        btnMultiply.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //tvEquation.append("\u00d7");
                tvEquation.append("*");
            }
        });

        btnLeftBrace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvEquation.append("(");
            }
        });

        btnRightBrace.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvEquation.append(")");
            }
        });

        btnExp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvEquation.append("");
            }
        });

        btnDot.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvEquation.append(".");
            }
        });

        btnEqual.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                tvAnswer.setText(tvEquation.getText().toString());
                eval(tvEquation.getText().toString());
            }
        });
        btnDel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String temp = tvEquation.getText().toString();
                if (temp.length() > 0)
                    tvEquation.setText(temp.substring(0, temp.length() - 1));
                else
                    Toast.makeText(rootView.getContext(), "Enter valid input", Toast.LENGTH_SHORT).show();
            }
        });

        btnDel.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                tvAnswer.setText("");
                tvEquation.setText("");
                return true;
            }
        });

        btnSteps.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(c==true) {
                    tvSteps.setVisibility(View.VISIBLE);
                    c=false;
                    btnSteps.setBackgroundResource(R.drawable.calculator);
                    tvAnswer.setText("");
                }
                else {
                    tvSteps.setVisibility(View.INVISIBLE);
                    c=true;
                    btnSteps.setBackgroundResource(R.drawable.steps);
                    tvSteps.setText("");
                    ds = "";
                }
            }
        });


        return rootView;

    }
    public static  double  applyOp(char op , double a, double b)
    {
        String s1,s2,s3,s4,s5="";     //for displaying the steps
        s1 = String.valueOf(a);
        s2 = String.valueOf(b);
        s3 = String.valueOf(op);

        if(ds.contains("("+s2+")"))
            s4 = "("+s2+")"+s3+s1;
        else if(ds.contains("("+s1+")"))
            s4 = s2+s3+"("+s1+")";
        else
            s4 = s2+s3+s1;
        DecimalFormat numberFormat = new DecimalFormat("#0.0");
        //System.out.println(numberFormat.format(number));
        switch (op)
        {
            case '+':
                s5 = String.valueOf(a+b);
                ds = ds.replace(s4,s5);
                tvSteps.append(" "+ds+"\n");
                return a + b;
            case '-':
                s5 = String.valueOf(b-a);
                ds = ds.replace(s4,s5);
                tvSteps.append(" "+ds+"\n");
                return b - a;
            case '*':
                s5 = String.valueOf(a*b);
                ds = ds.replace(s4,s5);
                tvSteps.append(" "+ds+"\n");
                return a * b;
            case '/':
                if (a != 0) {
                    s5 = numberFormat.format(b/a);
                    ds = ds.replace(s4,s5);
                    tvSteps.append(" "+ds+"\n");
                    return Double.parseDouble(numberFormat.format(b/a));
                }
            case '^':
                s5 = String.valueOf((int)Math.pow(b,a));
                ds = ds.replace(s4,s5);
                tvSteps.append(" "+ds+"\n");
                return (int)Math.pow(b,a);
        }
        return 0;
    }

    public static boolean hasPrecedence(char op1, char op2)
    {
        if (op2 == '(' || op2 == ')')
            return false;
        if(op2 == '^')
            return true;
        if ((op1 == '*' || op1 == '/') && (op2 == '+' || op2 == '-'))
            return false;
        else
            return true;
    }

    private void eval(String s) {

        //ds = s;


        boolean fl = true;
        char c[] = s.toCharArray();
        for(int i=0;i<c.length;i++)
        {
            if((c[i]>='0' && c[i]<='9') || c[i] =='.')
            {
                String sbuf = new String();
                while (i < c.length && ((c[i] >='0' && c[i] <= '9') || c[i] =='.'))
                {
                    sbuf = sbuf+c[i++];
                    fl = false;
                }
                if(sbuf.contains("."))
                    ds=ds+sbuf;
                else
                    ds=ds+sbuf+".0";
                if(fl == false)
                    i--;
            }
            else
                ds=ds+String.valueOf(c[i]);
        }


        tvSteps.append(" "+ds+"\n");
        //Log.i("Eq","eq:"+s);
        char tokens[] = s.toCharArray();
        boolean flag = true;
        Stack<Double> values = new Stack<>();

        Stack<Character> ops = new Stack<>();

        for(int i=0;i<tokens.length;i++)
        {
            if((tokens[i]>='0' && tokens[i]<='9') || tokens[i] =='.')
            {
                StringBuffer sbuf = new StringBuffer();
                while (i < tokens.length && ((tokens[i] >='0' && tokens[i] <= '9') || tokens[i] =='.'))
                {
                    sbuf.append(tokens[i++]);
                    flag = false;
                }
                values.push(Double.parseDouble(String.valueOf(sbuf)));
                if(flag == false)
                    i--;
                //Log.i("test","stack top:"+values.peek());
            }
            else if(tokens[i] == '(')
            {
                ops.push(tokens[i]);
                Log.i("test","ops"+ops.peek());
            }
            else if(tokens[i] == ')')
            {
                while(ops.peek() != '(')
                {
                    values.push(applyOp(ops.pop(),values.pop(),values.pop()));
                    Log.i("test","stack top:"+values.peek());
                }
                ops.pop();
            }
            else if (tokens[i] == '+' || tokens[i] == '-' ||
                    tokens[i] == '*' || tokens[i] == '/' || tokens[i] == '^') {
                while (!ops.empty() && hasPrecedence(tokens[i], ops.peek()))
                    values.push(applyOp(ops.pop(), values.pop(), values.pop()));
                ops.push(tokens[i]);
                Log.i("test","ops"+ops.peek());
            }
        }
        while (!ops.empty())
            values.push(applyOp(ops.pop(), values.pop(), values.pop()));
        Log.i("result","="+values.peek());
        tvAnswer.setText(values.peek().toString());
        //tvSteps.append(values.peek().toString());
    }
}
