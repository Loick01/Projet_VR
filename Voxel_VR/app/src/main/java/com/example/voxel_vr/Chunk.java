package com.example.voxel_vr;

import java.util.ArrayList;
import java.util.Random;
public class Chunk {
    private Voxel[] voxelsArray;
    private Random randomGenerator = new Random();
    private int numberOfFloat;
    final static int CHUNK_SIZE = 4;

    public Chunk(){
        this.voxelsArray = new Voxel[CHUNK_SIZE*CHUNK_SIZE*CHUNK_SIZE];
        this.numberOfFloat = this.voxelsArray.length * 108;
        for (int k = 0 ; k < CHUNK_SIZE ; k++){
            for (int j = 0 ; j < CHUNK_SIZE ; j++){
                for (int i = 0 ; i < CHUNK_SIZE ; i++){
                    this.voxelsArray[k*CHUNK_SIZE*CHUNK_SIZE+j*CHUNK_SIZE+i] = new Voxel(i,j,k,this.randomGenerator.nextInt(5));
                }
            }
        }
    }

    public int getNumberOfFloat(){
        return this.numberOfFloat;
    }

    public float[] getVertices(){
        ArrayList<Float> arrayList = new ArrayList<>();
        int compteur = 0;
        for (int v = 0 ; v < this.voxelsArray.length ; v++){
            float[] t = this.voxelsArray[v].getVertices();
            for (float value : t){
                ++compteur;
                arrayList.add(value);
            }
        }
        float[] res = new float[compteur];
        for (int i = 0 ; i < arrayList.size() ; i++) {
            res[i] = arrayList.get(i);
        }
        return res;
    }
}
