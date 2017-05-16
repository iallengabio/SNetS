package simulator;

import java.util.Hashtable;


public class Statistics {

    //----------------------------------------------------------------------------

    /**
     * Calcula o nivel de justica dos valores
     *
     * @param values double[]
     * @return double nivel de justica
     */
    public static double fairness(double[] values) {
        double numPairs = values.length;
        double media = 0;
        //deslocando o intervalo[0,1]->[1,2]
        for (int i = 0; i < numPairs; i++) {
            values[i] = values[i] + 1;
        }
        //calculando a media
        for (int i = 0; i < numPairs; i++) {
            media += values[i];
        }
        media = media / numPairs;
        //calculando a soma dos Xi/media...
        double sumXiDivMedia = 0;
        for (int i = 0; i < numPairs; i++) {
            sumXiDivMedia += values[i] / media;
        }
        //numerador
        double numerador = Math.pow(sumXiDivMedia, 2);
        //calculando a soma dos quadrados de Xi/media
        double sumXiDivMedia2 = 0;
        for (int i = 0; i < numPairs; i++) {
            sumXiDivMedia2 +=
                    Math.pow(values[i] / media, 2);
        }
        //denominador
        double denominador = numPairs * sumXiDivMedia2;

        return numerador / denominador;
    }

    //----------------------------------------------------------------------------

    /**
     * Retorna a media aritmetica.
     *
     * @param values double[]
     * @return double
     */
    public static double getAverage(double[] values) {
        double average = 0;
        double valuesNumber = values.length;

        for (int i = 0; i < (values.length); i++) {
            //retirando os valores NaN
            if (Double.isNaN(values[i])) {
                valuesNumber--;
            } else {
                average += values[i];
            }
        }
        average = average / valuesNumber;
        return average;
    }

    //----------------------------------------------------------------------------

    /**
     * Retorna o valor t da Tabela T-Student
     * Referente ao Nivel de Confianca e o Grau de liberdade(n-1)
     *
     * @param gl double Grau de liberdade
     * @param ns double Nivel de Significancia
     * @return double t
     */
    public static double getTStudent(double gl, double ns) {
        Hashtable<String, Double> tStudent = new Hashtable<String, Double>();
        //("grau de liberdade,nivel de significancia",t)
        //2 replicações
        tStudent.put("1.0,0.01", 63.657);
        tStudent.put("1.0,0.02", 31.82);
        tStudent.put("1.0,0.03", 21.205);
        tStudent.put("1.0,0.05", 12.706);
        //3 replicações
        tStudent.put("2.0,0.05", 4.303);
        //4 replicações
        tStudent.put("3.0,0.05", 3.182);
        //5 replicações
        tStudent.put("4.0,0.05", 2.776);
        //6 replicações
        tStudent.put("5.0,0.05", 2.571);
        //7 replicações
        tStudent.put("6.0,0.05", 2.447);
        //8 replicações
        tStudent.put("7.0,0.05", 2.365);
        //9 replicações
        tStudent.put("8.0,0.05", 2.306);
        //10 replicações
        tStudent.put("9.0,0.01", 3.250);
        tStudent.put("9.0,0.02", 2.821);
        tStudent.put("9.0,0.05", 2.262);
        //11 replicações
        tStudent.put("10.0,0.01", 3.169);
        tStudent.put("10.0,0.02", 2.764);
        tStudent.put("10.0,0.05", 2.228);
        //12 replicações
        tStudent.put("11.0,0.01", 3.106);
        tStudent.put("11.0,0.02", 2.718);
        tStudent.put("11.0,0.05", 2.201);
        //13 replicações
        tStudent.put("12.0,0.01", 3.055);
        tStudent.put("12.0,0.02", 2.681);
        tStudent.put("12.0,0.05", 2.179);
        //14 replicações
        tStudent.put("13.0,0.01", 3.012);
        tStudent.put("13.0,0.02", 2.650);
        tStudent.put("13.0,0.05", 2.160);
        //15 replicações
        tStudent.put("14.0,0.01", 2.977);
        tStudent.put("14.0,0.02", 2.624);
        tStudent.put("14.0,0.05", 2.145);
        //16 replicações
        tStudent.put("15.0,0.01", 2.947);
        tStudent.put("15.0,0.02", 2.602);
        tStudent.put("15.0,0.05", 2.131);
        //17 replicações
        tStudent.put("16.0,0.01", 2.921);
        tStudent.put("16.0,0.02", 2.583);
        tStudent.put("16.0,0.05", 2.120);
        //18 replicações
        tStudent.put("17.0,0.01", 2.898);
        tStudent.put("17.0,0.02", 2.567);
        tStudent.put("17.0,0.05", 2.110);
        //19 replicações
        tStudent.put("18.0,0.01", 2.878);
        tStudent.put("18.0,0.02", 2.552);
        tStudent.put("18.0,0.05", 2.101);
        //20 replicações
        tStudent.put("19.0,0.01", 2.861);
        tStudent.put("19.0,0.02", 2.539);
        tStudent.put("19.0,0.05", 2.093);
        //25 replicações
        tStudent.put("24.0,0.01", 2.797);
        tStudent.put("24.0,0.02", 2.492);
        tStudent.put("24.0,0.05", 2.064);
        //30 replicações
        tStudent.put("29.0,0.01", 2.797);
        tStudent.put("29.0,0.02", 2.492);
        tStudent.put("29.0,0.05", 2.064);
        String key = gl + "," + ns;

        if (tStudent.get(key) != null) {
            return ((double) tStudent.get(key));
        }
        //AMOSTRAS GRANDES
        if (ns == 0.05) {
            return 1.096;
        }
        if (ns == 0.02) {
            return 2.326;
        }
        if (ns == 0.01) {
            return 2.576;
        }
        return 1.096;//default->ns=0.05

    }

    //----------------------------------------------------------------------------

    /**
     * calculateDesvioPadrao
     *
     * @param values
     * @return
     */
    public static double calculateDesvioPadrao(int[] values) {
        double[] valuesD = new double[values.length];
        for (int i = 0; i < valuesD.length; i++) {
            valuesD[i] = values[i];
        }
        double avg = getAverage(valuesD);
        return calculateDesvioPadrao(valuesD, avg);
    }
    //----------------------------------------------------------------------------

    /**
     * Calcula o desvio padrão dos amostras
     *
     * @param values double[] valores das amostras
     * @param media  double media das amostras
     * @return double s
     */
    public static double calculateDesvioPadrao(double[] values, double media) {
        double n = values.length; //amostras

        double s; //desvio padrao

        double sumDesvio = 0; //soma dos desvios relativos a media

        /**
         * Soma das diferencas entre as amostras e a media elevado ao quadrado
         */
        for (int i = 0; i < values.length; i++) {
            //retira valores NaN
            if (Double.isNaN(values[i])) {
                n--;
            } else {
                sumDesvio += Math.pow(values[i] - media, 2);
            }
        }
        double variancia = sumDesvio / (n - 1);
        s = Math.sqrt(variancia);
        return s;
    }

//------------------------------------------------------------------------------

    /**
     * Calcula o erro para o intervalo de confiança da media
     *
     * @param values double[] valores das amostras
     * @param media  double media das amostras
     * @param ns     double Nivel de Significancia
     * @return double erro
     */
    public static double calculateError(double[] values, double media, double ns) {
        double n = values.length; //amostras
        //retirando os valores NaN

        for (int i = 0; i < values.length; i++) {
            if (Double.isNaN(values[i])) {
                n--;
            }
        }
        double t = getTStudent(n - 1, ns);
        double s; //desvio padrao

        s = Statistics.calculateDesvioPadrao(values, media);

        double erro = t * s / Math.sqrt(n);

        return erro;
    }

    /**
     * Retorna o tamanho min. da amostra.
     *
     * @param ns    double nivel de significancia
     * @param dp    double desvio padrao
     * @param error double erro desejado
     * @return double z
     */
    public static double calculateSampleSizeMin(double ns, double dp,
                                                double error) {
        double result;
        double z = getZ(ns);
        result = z * z * dp * dp / (error * error);
        return result;
    }

    /**
     * Retorna valor de z
     *
     * @param ns double nivel de significancia
     * @return double
     */
    public static double getZ(double ns) {
        Hashtable<Double, Double> TableZ = new Hashtable<Double, Double>();
        //(nivel de significancia,z)
        TableZ.put(0.5, 0.674); //0.5 de confiança

        TableZ.put(0.2, 1.282); //0.7 de confiança

        TableZ.put(0.1, 1.645); //0.9 de confiança

        TableZ.put(0.05, 1.960); //0.95 de confiança

        TableZ.put(0.02, 2.326); //0.98 de confiança

        TableZ.put(0.01, 2.576); //0.99 de confiança

        TableZ.put(0.005, 2.807); //0.995 de confiança

        TableZ.put(0.002, 3.090); //0.998 de confiança

        TableZ.put(0.001, 3.291); //0.999 de confiança

        double key = ns;
        if (TableZ.get(key) != null) {
            return ((double) TableZ.get(key));
        }
        //System.out.println(".......amostras/nivel de significancia nao tabelado! z = 1.96.......");
        return 1.096; //NS=0.05

    }

    //----------------------------------------------------------------------------

    /**
     * Retorna o maior valor.
     *
     * @param values double[]
     * @return double
     */
    public static double getMaxValue(double[] values) {
        double maxValue = -Double.MIN_VALUE;

        for (int i = 0; i < values.length; i++) {
            //desconsiderando os valores NaN
            if (!Double.isNaN(values[i])) {
                if (values[i] > maxValue) {
                    maxValue = values[i];
                }
            }
        }

        if (maxValue == Double.MIN_VALUE) {//todos valores sao NaN
            maxValue = Double.NaN;
        }
        return maxValue;
    }

    /**
     * Retorna o tempo total de simulacao em minutos
     *
     * @param initTimeMilliseconds Double
     * @return double
     */
    public static double calculateTotalTimeMinutes(Double initTimeMilliseconds) {
        double totalTime = System.currentTimeMillis() - initTimeMilliseconds;
        totalTime = (totalTime / 1000) / 60; //convertendo milesegundos->minutos=t/1000ms/60s

        return totalTime;
    }

    //........TESTES..............
    public static void main(String args[]) {
        /**
         * FONTE dos testes:
         * Barbetta, Reis e Bornia. Estatística para cursos de Eng. e Informática, 2004.
         */
        /*
    	Vector teste = new Vector();
    	teste.add(new String ("A"));
    	teste.add(new String ("B"));
    	teste.add(new String ("C"));
    	
    	teste.add(0,new String ("D"));
    	
    	for (int k=0;k<teste.size();k++){
    		System.out.println(teste.get(k));
    	}
    	System.out.println("----------------------------");
    	
    	for (int k=teste.size()-1;k>=0;k--){
    		
    		System.out.println(teste.get(k));
    		if (teste.get(k).equals("A"))
    			teste.remove(k);
    	}
    	
	   */
        //Teste 1
        double[] values = new double[10];
        int i = 0;
        values[i++] = 36.4;
        values[i++] = 35.7;
        values[i++] = 37.2;
        values[i++] = 36.5;
        values[i++] = 34.9;
        values[i++] = 35.2;
        values[i++] = 36.3;
        values[i++] = 35.8;
        values[i++] = 36.6;
        values[i++] = 36.9;
        double x = getAverage(values);
        double s = calculateDesvioPadrao(values, x);
        double error = calculateError(values, x, 0.05);
        System.out.println("Média=" + x); //deve ser 36,15

        System.out.println("DP=" + s); //deve ser 0.7352

        System.out.println("Error=" + error); //0.53
        //teste 2. dever ser igual a 39,8

        System.out.println("Tam. Mín. da amostra para erro 0.3 e NC 0.99 = " +
                calculateSampleSizeMin(0.01, s, 0.3));
    }
    //........TESTES..............
}
