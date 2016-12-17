	//近似反距离加权方法；
	/*
	 * 引入KDTree，效率在测试数据上快10多倍；
	 */
	public static float[] Cressman(float[] x, float[] y, float[] z, float[] xGrid, float[] yGrid, float radius){
		Euclidean<float[]> kdtree = new Euclidean<float[]>(2);
		int l = x.length;
		
		for(int i = 0; i < l; i++){
			//避免key重复；
			double nodex = x[i]+(Math.random()*10e-5);
			double nodey = y[i]+(Math.random()*10e-5);
			kdtree.addPoint(new double[]{nodex, nodey}, new float[]{x[i], y[i], z[i]});
		}

		int w = xGrid.length;
		int h = yGrid.length;
		
		float fillValue = -9999.9f;
		float[] grid = new float[w * h];
		for(int i = 0; i < h; i++){
			float yValue = yGrid[i];
			int lid = i * w;
			for(int j = 0;j < w; j++){
				float xValue = xGrid[j];
				int id = lid + j;
				List<float[]> nearPntList = kdtree.ballSearch(new double[]{xValue, yValue}, radius);
				int nearSize = nearPntList.size();
				if(nearSize < 1){
					grid[id] = fillValue;
					continue;
				}
				
				float z_sum = 0.0f;
				float weight_sum = 0.0f;
				for(int k = 0; k < nearSize; k++){
					float[] xyz = nearPntList.get(k);
					float distance = (float) Point2D.Float.distance(xValue, yValue, xyz[0], xyz[1]);
					if(distance <= radius && distance > 0){
						weight_sum += radius/distance;
						z_sum += radius/distance*xyz[2];
					}else if(Math.abs(distance) < 0.0001){	//格点在采样点上，取采样点值；
						z_sum = xyz[2];
						weight_sum = 1.0f;
						break;
					}
				}
				if(Math.abs(weight_sum) < 0.0001){
					grid[id] = fillValue;
				}else{
					grid[id] = z_sum/weight_sum;
				}
			}
		}
		return grid;
	}