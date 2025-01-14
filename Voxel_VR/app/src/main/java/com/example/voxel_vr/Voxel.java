package com.example.voxel_vr;

public class Voxel {
    private int center_x, center_y, center_z; // Position du centre du voxel
    float[] mVerticesData; // Sommets du voxel
    private int type;

    private void buildVoxel(){
        this.mVerticesData = new float[]{
                // Face inférieure
                -0.5F, -0.5F, -0.5F,
                0.5F, -0.5F, -0.5F,
                -0.5F, -0.5F, 0.5F,

                -0.5F, -0.5F, 0.5F,
                0.5F, -0.5F, -0.5F,
                0.5F, -0.5F, 0.5F,

                // Face supérieure
                -0.5F, 0.5F, 0.5F,
                0.5F, 0.5F, 0.5F,
                -0.5F, 0.5F, -0.5F,

                -0.5F, 0.5F, -0.5F,
                0.5F, 0.5F, 0.5F,
                0.5F, 0.5F, -0.5F,

                // Face arrière
                0.5F, -0.5F, -0.5F,
                -0.5F, -0.5F, -0.5F,
                0.5F, 0.5F, -0.5F,

                0.5F, 0.5F, -0.5F,
                -0.5F, -0.5F, -0.5F,
                -0.5F, 0.5F, -0.5F,

                // Face avant
                -0.5F, -0.5F, 0.5F,
                0.5F, -0.5F, 0.5F,
                -0.5F, 0.5F, 0.5F,

                -0.5F, 0.5F, 0.5F,
                0.5F, -0.5F, 0.5F,
                0.5F, 0.5F, 0.5F,

                // Face gauche
                -0.5F, -0.5F, -0.5F,
                -0.5F, -0.5F, 0.5F,
                -0.5F, 0.5F, -0.5F,

                -0.5F, 0.5F, -0.5F,
                -0.5F, -0.5F, 0.5F,
                -0.5F, 0.5F, 0.5F,

                // Face droite
                0.5F, -0.5F, 0.5F,
                0.5F, -0.5F, -0.5F,
                0.5F, 0.5F, 0.5F,

                0.5F, 0.5F, 0.5F,
                0.5F, -0.5F, -0.5F,
                0.5F, 0.5F, -0.5F
        };

        // On met à jour la position du voxel en fonction de la position de son centre
        for (int n = 0 ; n < this.mVerticesData.length ; n++){
            int curr = n%3;
            if (curr==0){
                this.mVerticesData[n] += this.center_x;
            }else if(curr==1){
                this.mVerticesData[n] += this.center_y;
            }else if(curr==2){
                this.mVerticesData[n] += this.center_z;
            }
        }
    }

    public Voxel(int center_x, int center_y, int center_z, int type) {
        this.center_x = center_x;
        this.center_y = center_y;
        this.center_z = center_z;
        this.type = type; // Bloc de pierre par défaut
        this.buildVoxel();
    }

    public float[] getVertices(){
        return this.mVerticesData;
    }
}
