import java.util.*;
import javax.swing.text.DefaultStyledDocument.ElementSpec;
import java.lang.Math;
import java.math.BigDecimal;
import java.io.File;
import java.text.DecimalFormat;
import java.io.FileInputStream;

public class sim
{
static int mispredictions=0;
static double n_bit_value;
static double input_bits;
static double input_bits_g;
static double pc_bits;
static double cur_bit;
static int prediction=0;
static List<String> taken=new ArrayList<String>();
static List<String> branches=new ArrayList<String>();
static double chooser_bits;

// SMITH N-BIT PREDICTOR
public static int smith(String s1,String s2,String s3) 
    { 
    mispredictions=0;
    n_bit_value=Math.pow(2,input_bits);
    cur_bit=Math.pow(2,input_bits-1);   
    for (int i=0;i<branches.size();i++)
    {
     if(taken.get(i).equals("t"))
     {
      if(cur_bit<Math.pow(2,(int)input_bits-1)) 
      {
       mispredictions+=1;
      }
      cur_bit+=1;                 //increasing the value if the branch is taken
      if(cur_bit>n_bit_value-1)
      {
          cur_bit=n_bit_value-1;
      }
      }
     else if(taken.get(i).equals("n"))
     {
      if (cur_bit>=Math.pow(2,(int)input_bits-1))
      {
        mispredictions+=1;
      }
      cur_bit-=1; //decreasing the value if the branch is not taken
      if (cur_bit<0)
      {
       cur_bit=0;
      }
     } 
    }
    System.out.println("COMMAND");
    System.out.println("./sim "+s1+" "+s2+" "+s3);
    System.out.println("OUTPUT");
    System.out.print("number of predictions:        ");
    System.out.println(branches.size());
    System.out.print("number of mispredictions:     ");
    System.out.println(mispredictions);
    System.out.print("misprediction rate:           ");
    System.out.println(String.format("%.2f",new BigDecimal(Float.toString((float)mispredictions/branches.size()*100)))+"%");
    System.out.print("FINAL COUNTER CONTENT:        ");
    System.out.println((int)cur_bit);
    return 0;
}

// BIMODAL BRANCH PREDICTOR
public static int bimodal(String s1,String s2,String s3)
{ 
  mispredictions=0;
  n_bit_value=Math.pow(2,(int)input_bits);
  double[] predtable=new double[(int)n_bit_value];
  for (int i=0;i<(int)n_bit_value;i++)
  {
    predtable[i]=4;
  }
  for(int i=0;i<branches.size();i++)
  {
    String branchpc=Integer.toBinaryString(Integer.parseInt(branches.get(i),16)); // converts hexcode to binary string
    int n=branchpc.length();
    int index=Integer.parseInt(branchpc.substring(n-((int)input_bits)-2,n-2),2); //calculating bimodal index
    if (taken.get(i).equals("t"))
    {
      if (predtable[index]<4)
      {
        mispredictions+=1;   
      }
      predtable[index]+=1;             //increasing the prediction value at that index if the branch is taken
      if (predtable[index]>7)
      {
        predtable[index]=7;
      }
    }
    if (taken.get(i).equals("n"))
    {
      if (predtable[index]>=4)
      {
        mispredictions+=1;
      }
      predtable[index]-=1;           //decreasing the prediction value at that index if the branch is not taken
      if (predtable[index]<0)
      {
        predtable[index
        ]=0;
      }
    }
  }
  System.out.println("COMMAND");
  System.out.println("./sim "+s1+" "+s2+" "+s3);
  System.out.println("OUTPUT");
  System.out.print("number of predictions:        ");
  System.out.println(branches.size());
  System.out.print("number of mispredictions:     ");
  System.out.println(mispredictions);
  System.out.print("misprediction rate:           ");
  System.out.println(String.format("%.2f",new BigDecimal(Float.toString((float)mispredictions/branches.size()*100)))+"%");
  System.out.println("FINAL BIMODAL CONTENTS");
  for (int i=0;i<predtable.length;i++)
  {
    System.out.print(i+"	");
    System.out.println((int)predtable[i]);

  }
  return 0;
}

//GSHARE BRANCH PREDICTOR
public static int gshare(String s1,String s2,String s3,String s4)
{ 
  mispredictions=0;
  n_bit_value=Math.pow(2, (int)pc_bits);
  double[] predtable=new double[(int)n_bit_value];
  ArrayList<String> gbhregister=new ArrayList<String>();
  for (int i=0;i<(int)input_bits_g;i++)                 // initialising the global branch history register
  {
    gbhregister.add("0");
  }
  for (int i=0;i<n_bit_value;i++)
  {
    predtable[i]=4;
  }
  for(int i=0;i<branches.size();i++)
  { 
    String gbhr="";
    for (int j=0;j<(int)input_bits_g;j++)
    { 
      gbhr+=gbhregister.get(j);
    }
    String branchpc=Integer.toBinaryString(Integer.parseInt(branches.get(i),16));   //converting the hexcode to binary string
    int n=branchpc.length();
    String pcv=branchpc.substring(n-((int)pc_bits)-2,n-2).substring((int)pc_bits-(int)input_bits_g);
    String addr=branchpc.substring(n-((int)pc_bits)-2,n-2).substring(0,((int)pc_bits-(int)input_bits_g));
    String xored="";
    for (int j=0;j<(int)input_bits_g;j++)   // xor of last n bits of pc and gbhr
    {
    if(pcv.charAt(j)==gbhr.charAt(j))
    {
      xored+="0";
    }
    else
    {
      xored+="1";
    }
   }
   int index=Integer.parseInt(addr+xored,2);   // calculating the Gshare index
   
    if (taken.get(i).equals("t"))
    {
      if (predtable[index]<4)
      {
        mispredictions+=1;                          
      }
      predtable[index]+=1;
      if (predtable[index]>7)
      {
        predtable[index]=7;
      }
      gbhregister.add(0,"1");         //updating the gbhr by makibng right bitwise shift and updating the most significant bit to'1'
      gbhregister.remove((int)input_bits_g);         
    }
    if (taken.get(i).equals("n"))
    {
      if (predtable[index]>=4)
      {
        mispredictions+=1;
      }
      predtable[index]-=1;
      if (predtable[index]<0)
      {
        predtable[index]=0;
      }
      gbhregister.add(0,"0");      //updating the gbhr by making right bitwise shift and updating the most significant bit to'0'
      gbhregister.remove((int)input_bits_g);
    }
  } 
  System.out.println("COMMAND");
  System.out.println("./sim "+s1+" "+s2+" "+s3+" "+s4);
  System.out.println("OUTPUT");
  System.out.print("number of predictions:        ");
  System.out.println(branches.size());
  System.out.print("number of mispredictions:     ");
  System.out.println(mispredictions);
  System.out.print("misprediction rate:           ");
  System.out.println(String.format("%.2f",new BigDecimal(Float.toString((float)mispredictions/branches.size()*100)))+"%");
  System.out.println("FINAL GSHARE CONTENTS");
  for (int i=0;i<predtable.length;i++)
  {
    System.out.print(i+"	");
    System.out.println((int)predtable[i]);
  }
  return 0;
}


// HYBRID BRANCH PREDICTOR
public static int hybrid(String s1,String s2,String s3,String s4,String s5,String s6)
{
  mispredictions=0;
  n_bit_value=Math.pow(2,input_bits);
  int[] predtable_b=new int[(int)n_bit_value];
  for (int i=0;i<(int)n_bit_value;i++)
  {
    predtable_b[i]=4;
  }
  int b_index;
  int[] predtable_g=new int[(int)Math.pow(2,pc_bits)];
  for (int i=0;i<(int)Math.pow(2,pc_bits);i++)              //initialising the gshare prediction table
  {
    predtable_g[i]=4;
  }
  ArrayList<String> gbhregister=new ArrayList<String>();
  for (int i=0;i<(int)input_bits_g;i++)
  {
    gbhregister.add("0");                                  // initialising the gbhr
  }
  int g_index;
  int[] choosertable=new int[(int)Math.pow(2,chooser_bits)];
  for (int i=0;i<(int)Math.pow(2,chooser_bits);i++)
  {
    choosertable[i]=1;                                       // initialising the chooser table
  }
  for(int i=0;i<branches.size();i++)
  {
    String gbhr="";
    for (int j=0;j<(int)input_bits_g;j++)
    { 
      gbhr+=gbhregister.get(j);
    }
    String branchpc=Integer.toBinaryString(Integer.parseInt(branches.get(i),16));  // converting the hexcode to binary
    int n=branchpc.length();
    int hybridindex=Integer.parseInt(branchpc.substring(n-((int)chooser_bits)-2,n-2),2); //calculates hybrid index
    
    String pcv=branchpc.substring(n-((int)pc_bits)-2,n-2).substring((int)pc_bits-(int)input_bits_g);
    String addr=branchpc.substring(n-((int)pc_bits)-2,n-2).substring(0,((int)pc_bits-(int)input_bits_g));
    String xored="";
     for (int j=0;j<(int)input_bits_g;j++)
     {
      if(pcv.charAt(j)==gbhr.charAt(j))
       { 
        xored+="0";
       }
      else
      {
       xored+="1";
      }
    }
    g_index=Integer.parseInt(addr+xored,2);  //Gshare index for hybrid
    b_index=Integer.parseInt(branchpc.substring(n-((int)input_bits)-2,n-2),2); //Bimodal index for hybrid
    int g=predtable_g[g_index];
    int b=predtable_b[b_index]; 
    if (taken.get(i).equals("t"))
    {
      if (choosertable[hybridindex]>=2)             // checking whether we have to take the gshare prediction or the bimodal prediction
      {
        if (predtable_g[g_index]<4)                 //case where we take gshare prediction
        {
          mispredictions+=1;
        }

        predtable_g[g_index]+=1; 
      if (predtable_g[g_index]>7)                  // updating the gshare prediction table
      {
        predtable_g[g_index]=7;
      }
      }
      else if(choosertable[hybridindex]<2)       //we have to take bimodal prediction
      {
        if (predtable_b[b_index]<4)
        {
          mispredictions+=1;
        }
        predtable_b[b_index]+=1; 
      if (predtable_b[b_index]>7)
      {
        predtable_b[b_index]=7;
      }
      }
      gbhregister.add(0,"1");    // we update the gbhr irrespective of the choosertable value
      gbhregister.remove((int)input_bits_g);

      if (b>=4 && g<4) 
      {
       choosertable[hybridindex]-=1;           // we update the choosertable by decreasing the value when bimodal is correct and gshare is incorrect
       if (choosertable[hybridindex]<0)
       {
        choosertable[hybridindex]=0;
       }
      }
      else if (b<4 && g>=4)
      {
        choosertable[hybridindex]+=1;           //we update the choosertable by increasing the value when bimodal is incorrect and gshare is correct
        if(choosertable[hybridindex]>3)
        {
          choosertable[hybridindex]=3;
        }
      }

    }
    else if(taken.get(i).equals("n"))
    {
      if (choosertable[hybridindex]>=2)
      {
        if (predtable_g[g_index]>=4)
        {
          mispredictions+=1;
        }
      predtable_g[g_index]-=1;
      if (predtable_g[g_index]<0)
      {
        predtable_g[g_index]=0;
      }
     }

     else if(choosertable[hybridindex]<2)
     {
      if (predtable_b[b_index]>=4)
        {
          mispredictions+=1;
        }
      predtable_b[b_index]-=1;
      if (predtable_b[b_index]<0)
      {
        predtable_b[b_index]=0;
      }
     }
     gbhregister.add(0,"0");
     gbhregister.remove((int)input_bits_g);

     if (b>=4 && g<4) 
      {
        choosertable[hybridindex]+=1;
        if(choosertable[hybridindex]>3)
        {
          choosertable[hybridindex]=3;
        }
      }
      else if (b<4 && g>=4)
      {
        choosertable[hybridindex]-=1;
       if (choosertable[hybridindex]<0)
       {
        choosertable[hybridindex]=0;
       }
      }
    }

    
  }
  System.out.println("COMMAND");
  System.out.println("./sim "+s1+" "+s2+" "+s3+" "+s4+" "+s5+" "+s6);
  System.out.println("OUTPUT");
  System.out.print("number of predictions:        ");
  System.out.println(branches.size());
  System.out.print("number of mispredictions:     ");
  System.out.println(mispredictions);
  System.out.print("misprediction rate:           ");
  System.out.println(String.format("%.2f",new BigDecimal(Float.toString((float)mispredictions/branches.size()*100)))+"%");
  System.out.println("FINAL CHOOSER CONTENTS");
  for (int i=0;i<choosertable.length;i++)
  {
    System.out.print(i+"	");
    System.out.println(choosertable[i]);
  }
  System.out.println("FINAL GSHARE CONTENTS");
  for (int i=0;i<predtable_g.length;i++)
  {
    System.out.print(i+"	");
    System.out.println(predtable_g[i]);
  }
  System.out.println("FINAL BIMODAL CONTENTS");
  for (int i=0;i<predtable_b.length;i++)
  {
    System.out.print(i+"	");
    System.out.println(predtable_b[i]);
  }
  return 0;
}

// MAIN FUNCTION
    public static void main(String args[])
    {     
          switch(args[1])
          {
            case "smith":
            try{
              FileInputStream f=new FileInputStream(args[3]);    // reading the trace file 
              Scanner s=new Scanner(f);
              while(s.hasNextLine())
              { 
                String[] bt=s.nextLine().toString().split(" ");
                taken.add(bt[1]);                               // adding taken data to taken array
                branches.add(bt[0]);                            // adding branch address to branch array
              }
              s.close();
            }
            catch(Exception e)
            {
                System.out.println(e);
            }
            input_bits=Double.parseDouble(args[2]);
            smith(args[1],args[2],args[3]);
            break;
            case "bimodal":
            try{
              FileInputStream f=new FileInputStream(args[3]);
              Scanner s=new Scanner(f);
              while(s.hasNextLine())
              { 
                String[] bt=s.nextLine().toString().split(" ");
                taken.add(bt[1]);
                branches.add(bt[0]);
              }
              s.close();
            }
            catch(Exception e)
            {
                System.out.println(e);
            }
            input_bits=Double.parseDouble(args[2]);
            bimodal(args[1],args[2],args[3]);
            break;
            case "gshare":
            try{
              FileInputStream f=new FileInputStream(args[4]);
              Scanner s=new Scanner(f);
              while(s.hasNextLine())
              { 
                String[] bt=s.nextLine().toString().split(" ");
                taken.add(bt[1]);
                branches.add(bt[0]);
              }
              s.close();
            }
            catch(Exception e)
            {
                System.out.println(e);
            }
            pc_bits=Double.parseDouble(args[2]);
            input_bits_g=Double.parseDouble(args[3]);
            gshare(args[1],args[2],args[3],args[4]);
            break;
            case "hybrid":
            try{
            FileInputStream f=new FileInputStream(args[6]);
              Scanner s=new Scanner(f);
              while(s.hasNextLine())
              { 
                String[] bt=s.nextLine().toString().split(" ");
                taken.add(bt[1]);
                branches.add(bt[0]);
              }
              s.close();
            }
            catch(Exception e)
            {
                System.out.println(e);
            }
            chooser_bits=Double.parseDouble(args[2]);
            pc_bits=Double.parseDouble(args[3]);
            input_bits_g=Double.parseDouble(args[4]);
            input_bits=Double.parseDouble(args[5]);
            hybrid(args[1],args[2],args[3],args[4],args[5],args[6]);
          }
    }
}
