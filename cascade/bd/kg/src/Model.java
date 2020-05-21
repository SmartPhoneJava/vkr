import java.util.List;
import java.util.Map;

import java.io.BufferedReader;
import java.io.FileReader;

import java.util.ArrayList;
import java.util.HashMap;
import java.io.IOException;

public class Model
{
	private List<Vertex>  m_vertices;
	private List<Integer> m_indices;
	
	public Vector m_center;
	
	public float middle_distance;
	
	public float middle_flat_distance;
	
	public float middle_flat_distance_save;
	
	
	private static String[] RemoveEmptyStrings(String[] data)
	{
		List<String> result = new ArrayList<String>();
		
		for(int i = 0; i < data.length; i++)
			if(!data[i].equals(""))
				result.add(data[i]);
		
		String[] res = new String[result.size()];
		result.toArray(res);
		
		return res;
	}
	
	private class ModelIndex
	{
		private int vertex;
		private int texture;
		private int normal;

		public int GetVertexIndex()   { return vertex; }
		public int GetTexCoordIndex() { return texture; }
		public int GetNormalIndex()   { return normal; }

		public void SetVertexIndex(int val)   { vertex = val; }
		public void SetTexCoordIndex(int val) { texture = val; }
		public void SetNormalIndex(int val)   { normal = val; }

		@Override
		public boolean equals(Object obj)
		{
			ModelIndex index = (ModelIndex)obj;

			return vertex == index.vertex
					&& texture == index.texture
					&& normal == index.normal;
		}

		@Override
		public int hashCode()
		{
			final int BASE = 17;
			final int MULTIPLIER = 31;

			int result = BASE;

			result = MULTIPLIER * result + vertex;
			result = MULTIPLIER * result + texture;
			result = MULTIPLIER * result + normal;

			return result;
		}
	}
	
	private ModelIndex ParseOBJIndex(String token)
	{
		String[] values = token.split("/");

		ModelIndex result = new ModelIndex();
		result.SetVertexIndex(Integer.parseInt(values[0]) - 1);

		if(values.length > 1)
		{
			if(!values[1].isEmpty())
			{
				result.SetTexCoordIndex(Integer.parseInt(values[1]) - 1);
			}

			if(values.length > 2)
			{
				result.SetNormalIndex(Integer.parseInt(values[2]) - 1);
			}
		}

		return result;
	}
	
	public Model(String fileName) throws IOException
	{
		m_vertices = new ArrayList<Vertex>();
		m_indices = new ArrayList<Integer>();
		ArrayList<ModelIndex> class_indices = new ArrayList<ModelIndex>();
		boolean m_hasTexCoords = false;
		boolean m_hasNormals = false;
		
		ArrayList<Vector> m_positions = new ArrayList<Vector>();
		ArrayList<Vector> m_texCoords = new ArrayList<Vector>();
		ArrayList<Vector> m_normals = new ArrayList<Vector>();

		BufferedReader meshReader = null;

		meshReader = new BufferedReader(new FileReader(fileName));
		String line;

		while((line = meshReader.readLine()) != null)
		{
			String[] tokens = line.split(" ");
			tokens = RemoveEmptyStrings(tokens);

			if(tokens.length == 0 || tokens[0].equals("#"))
				continue;
			else if(tokens[0].equals("v"))
			{
				m_positions.add(new Vector(Float.valueOf(tokens[1]),
						Float.valueOf(tokens[2]),
						Float.valueOf(tokens[3]),1));
			}
			else if(tokens[0].equals("vt"))
			{
				m_hasTexCoords = true;
				m_texCoords.add(new Vector(Float.valueOf(tokens[1]),
						1.0f - Float.valueOf(tokens[2]),0,0));
			}
			else if(tokens[0].equals("vn"))
			{
				m_hasNormals = true;
				m_normals.add(new Vector(Float.valueOf(tokens[1]),
						Float.valueOf(tokens[2]),
						Float.valueOf(tokens[3]),0));
			}
			else if(tokens[0].equals("f"))
			{
				for(int i = 0; i < tokens.length - 3; i++)
				{
					class_indices.add(ParseOBJIndex(tokens[1]));
					class_indices.add(ParseOBJIndex(tokens[2 + i]));
					class_indices.add(ParseOBJIndex(tokens[3 + i]));
				}
			}
		}
		meshReader.close();
		
		Map<ModelIndex, Integer> resultIndexMap = new HashMap<ModelIndex, Integer>();

		//System.out.println("m_indices.size()" + m_indices.size());
		for(int i = 0; i < class_indices.size(); i++)
		{
			ModelIndex currentIndex = class_indices.get(i);

			Vector currentPosition = m_positions.get(currentIndex.GetVertexIndex());
			Vector currentTexCoord;
			Vector currentNormal;

			if(m_hasTexCoords)
				currentTexCoord = m_texCoords.get(currentIndex.GetTexCoordIndex());
			else
				currentTexCoord = new Vector(0,0,0,0);

			if(m_hasNormals)
				currentNormal = m_normals.get(currentIndex.GetNormalIndex());
			else
				currentNormal = new Vector(0,0,0,0);

			Integer modelVertexIndex = resultIndexMap.get(currentIndex);

			if(modelVertexIndex == null)
			{
				modelVertexIndex = m_vertices.size();
				resultIndexMap.put(currentIndex, modelVertexIndex);
				
				m_vertices.add(new Vertex(
						currentPosition,
						currentTexCoord,
						currentNormal));
			}
			

			m_indices.add(modelVertexIndex);
		}
		countCenter();
		System.out.println("size is " + m_vertices.size());
	}
	
	private void countCenter() {
		int amount = m_vertices.size();
		
		float x_count = 0.0f;
		float y_count = 0.0f;
		float z_count = 0.0f;
		
		float distance = 0;
		float fdistance = 0;
		Vector cen = new Vector(0,0,0);
		
		for(int i = 0; i < amount; i++)
		{
			x_count +=m_vertices.get(i).GetX();
			y_count +=m_vertices.get(i).GetY();
			z_count +=m_vertices.get(i).GetZ();
		}
		
		if (amount > 0) {
			m_center = new Vector(x_count/amount,
					y_count/amount,
					z_count/amount);
			cen.Div(amount);
		} else {
			m_center = new Vector(0,0,0);
		}
		
		for(int i = 0; i < amount; i++)
		{
			fdistance += m_vertices.get(i).GetPosition().flat_distance_squared(m_center);
			distance += m_vertices.get(i).GetPosition().distance_squared(m_center);
		}
		
		if (amount > 0) {
			distance /= amount;
			fdistance /= amount;
		} 
		
		middle_distance = distance;
		middle_flat_distance = fdistance;
		middle_flat_distance_save = fdistance;
	}
	
	public float deform(Vertex v, float mfd, float get_k) {
		float v_y = v.GetY();
		float yy = v_y;
		
		int before = 0;
		int after = 0;
		
		//boolean for_return = false;
		if (v.GetPosition().flat_distance_squared(m_center) < mfd) {
			
			float center_y = m_center.GetY();
			float close = (4 * get_k);
			
			
			if (get_k >= 0) {
				//if (get_k < 0.5f) {
				//	close *= 3 * (v.GetPosition().flat_distance_squared(m_center) / mfd);
				//} else {
					close /= 3*(v.GetPosition().flat_distance_squared(m_center) / mfd);
				//}
				if (v_y >  center_y) {
					before = 1;
					yy = v_y - close;
				} else {
					before = -1;
					yy = v_y + close;
				}
				
				if (yy < center_y) {
					after = -1;
				} else {
					after = 1;
				}
				
				if (before * after < 0) {
					if (get_k > 0) {
						yy = center_y;
					}
				}
			}
			else if (get_k < 0) {
				close *= 5;
				//close /= (v.GetPosition().flat_distance_squared(m_center) / mfd);
				if (v_y >  center_y) {
					yy = v_y - close;
					if (yy > (center_y*1.8f)) {
						yy = center_y*1.8f;
						//System.out.println("yy < (center_y*2):"+(yy < (center_y*2))+"yy:"+yy + "; 2 * center_y:" + 2 * center_y + "; close:" + close);
					}
				} else {
					yy = v_y + close;
					if (yy < 0 * center_y) {
						yy = center_y*0f;
					}
				}
			}
			//System.out.println("yy:"+yy + "; 2 * center_y:" + 2 * center_y);
		}
		return yy;
	}
	
	public void Draw(RenderContext context, Matrix viewProjection,
			Transform transform, Bitmap texture, float get_k,
			float dark, int lighter)
	{
		Matrix matrix = transform.GetTransformation();
		
		Matrix mvp = viewProjection.Mul(matrix);

		float down = m_center.GetY();
		
		int first;
		int second;
		int third;
		
		//System.out.println("middle_flat_distance:" + middle_flat_distance);
		//middle_flat_distance -= 5;
		//else if (what_to_do == 0) {
		//	middle_flat_distance = middle_flat_distance_save * k;
		//}
		//System.out.println("k=" + k);
		
		
		float mfd;
		if (get_k < 0.01f) {
			mfd = middle_flat_distance_save;
		} else {
			mfd = 1.5f * middle_flat_distance_save;
		}
		
		
		//Transform t = new Transform(new Vector4f(0, 0, 0));
		//t.m_scale = new Vector4f(1.f, 0.f, 1.f);
		//Matrix4f m = t.GetTransformation();
		
		/*
		if (get_k < 0) {
			;
		}
		else if (get_k < 0.02f) {
			get_k = 0.f;
		}
		else if (get_k < 0.03f) {
			get_k = 0.01f;
		} 
		else if (get_k < 0.05){
			get_k = 0.03f;
		}
		else if (get_k < 0.09){
			get_k = 0.05f;
		}
		else if (get_k < 0.135){
			get_k = 0.09f;
		}
		else {
			get_k = 0.115f;
		}*/
		for(int i = 0; i < m_indices.size() - 2; i += 3)
		{
			
			Vertex v1 = m_vertices.get(m_indices.get(i));
			Vertex v2 = m_vertices.get(m_indices.get(i + 1));
			Vertex v3 = m_vertices.get(m_indices.get(i + 2));
			
			Vertex vv1 = v1, vv2 = v2, vv3 = v3;
			
			first = 0;
			second = 0;
			third = 0;
			
			float yy = 0;
			float close = 16;
			
			//System.out.println("get_k:" + get_k);
			
				
			yy = deform(v1, mfd, get_k);
			vv1 = new Vertex(new Vector(v1.GetX(), yy, 
					v1.GetZ()), v1.GetTexCoords(),
					v1.GetNormal());
			if (Math.abs(yy - down) < 0.01) {
				first = 1;
			}
			
			yy = deform(v2, mfd, get_k);
			vv2 = new Vertex(new Vector(v2.GetX(), yy, 
					v2.GetZ()), v2.GetTexCoords(),
					v2.GetNormal());
			if (Math.abs(yy - down) < 0.01) {
				second = 1;
			}
			
			yy = deform(v3, mfd, get_k);
			vv3 = new Vertex(new Vector(v3.GetX(), yy, 
					v3.GetZ()), v3.GetTexCoords(),
					v3.GetNormal());
			if (Math.abs(yy - down) < 0.01) {
				third = 1;
			}
			
			if ((first + second + third < 3) || (get_k < 0))
			{
				context.DrawTriangle(
						vv1.Transform(mvp, matrix),
						vv2.Transform(mvp, matrix),
						vv3.Transform(mvp, matrix),
						texture, dark, lighter);
				
			}
			
			
			//System.out.println("v3.GetY()=" + v3.GetY());
			/*
			if (v1.GetPosition().flat_distance_squared(m_center) < mfd) {
			
				float v_y = v1.GetY();
				float center_y = m_center.GetY();
				if (v_y > center_y) {
					yy = v_y - close;
				} else {
					yy = v_y + close;
				}
				if ((yy -center_y) * (v_y - center_y) < 0) {
					yy = down;
					first = 1;
				}
				vv1 = new Vertex(new Vector4f(v1.GetX(), yy, 
						v1.GetZ()), v1.GetTexCoords(),
						v1.GetNormal());
			}
			*/
			
			//if (v2.GetPosition().flat_distance_squared(m_center) < mfd) {
			//if (Math.abs(v2.GetPosition().GetX() - m_center.GetX()) < 2) {
			//if (v2.GetPosition().distance_squared(m_center) < middle_distance) {
				/*
				if (v1.GetY() > m_center.GetY()) {
					vv2 = new Vertex(new Vector4f(v2.GetX(), down, v2.GetZ()),
							new Vector4f(0,0,0), new Vector4f(0,0,0));
				} else {
					vv2 = new Vertex(new Vector4f(v2.GetX(), down, v2.GetZ()),
							new Vector4f(0,0,0), new Vector4f(0,0,0));
				}
				*/
			/*
				if (v2.GetY() > m_center.GetY()) {
					yy = v2.GetY() - close;
				} else {
					yy = v2.GetY() + close;
				}
				if ((yy - m_center.GetY()) * (v2.GetY() - m_center.GetY()) < 0) {
					yy = down;
					second = 1;
				}
				vv2 = new Vertex(new Vector4f(v2.GetX(), yy, v2.GetZ()), v2.GetTexCoords(),
						v2.GetNormal());
				//vv2 = vv2.Transform(mvp, m);
				
			}
			
			if (v3.GetPosition().flat_distance_squared(m_center) < mfd) {
			*/
			//if (Math.abs(v3.GetPosition().GetX() - m_center.GetX()) < 2) {
			//if (v3.GetPosition().distance_squared(m_center) < middle_distance) {
				/*
				if (v1.GetY() > m_center.GetY()) {
					vv3 = new Vertex(new Vector4f(v3.GetX(), down, v3.GetZ()),
							new Vector4f(0,0,0), new Vector4f(0,0,0));
				} else {
					vv3 = new Vertex(new Vector4f(v3.GetX(), down, v3.GetZ()),
							new Vector4f(0,0,0), new Vector4f(0,0,0));
				}
				*/
			/*
				if (v3.GetY() > m_center.GetY()) {
					yy = v3.GetY() - close;
				} else {
					yy = v3.GetY() + close;
				}
				if ((yy - m_center.GetY()) * (v3.GetY() - m_center.GetY()) < 0) {
					yy = down;
					third = 1;
				}
				vv3 = new Vertex(new Vector4f(v3.GetX(), yy, v3.GetZ()), v3.GetTexCoords(),
						v3.GetNormal());
				//vv3 = vv3.Transform(mvp, m);
				
			}
			*/
			/*
			if (!(
					Math.abs(vv3.GetPosition().GetY() - vv1.GetPosition().GetY()) < 0.01
					&&
					Math.abs(vv1.GetPosition().GetY() - vv2.GetPosition().GetY()) < 0.01
					&&
					Math.abs(vv2.GetPosition().GetY() - vv3.GetPosition().GetY()) < 0.01
					))
			
			
			if (!(
					Math.abs(vv3.GetPosition().GetY() - down) < 0.001
					&&
					Math.abs(vv1.GetPosition().GetY() - down) < 0.001
					&&
					Math.abs(vv2.GetPosition().GetY() - down) < 0.001
					))
			*/
			
			
			//else if (t < 1800) {
				//System.out.println("t = " + t);
				//context.DrawTriangle(
				//		vv1.Transform(mvp, matrix),
				//		vv2.Transform(mvp, matrix),
				//		vv3.Transform(mvp, matrix),
				//		texture);
				//t++;
			//}
			
			//if (yep) {
			//	down--;
			//}
			/*
			if (m_vertices.get(m_indices.get(i)).GetPosition().distance_squared(m_center) > middle_distance) {
			context.DrawTriangle(
					m_vertices.get(m_indices.get(i)).Transform(mvp, matrix),
					m_vertices.get(m_indices.get(i + 1)).Transform(mvp, matrix),
					m_vertices.get(m_indices.get(i + 2)).Transform(mvp, matrix),
					texture);
			} else {
				if (m_vertices.get(m_indices.get(i)).GetPosition().GetY() < m_center.GetY()) {
					context.DrawTriangle(
							m_vertices.get(m_indices.get(i)).Transform(mvp, matrix),
							m_vertices.get(m_indices.get(i + 1)).Transform(mvp, matrix),
							m_vertices.get(m_indices.get(i + 2)).Transform(mvp, matrix),
							texture);
				}
				else {
					context.DrawTriangle(
							m_vertices.get(m_indices.get(i)).Transform(mvp, matrix),
							m_vertices.get(m_indices.get(i + 1)).Transform(mvp, matrix),
							m_vertices.get(m_indices.get(i + 2)).Transform(mvp, matrix),
							texture);
					
					Transform transform1 = new Transform(new Vector4f(0, down, 0.0f));
					Transform transform2 = new Transform(new Vector4f(0, down, 0.0f));
					Transform transform3 = new Transform(new Vector4f(0, down, 0.0f));
					Matrix4f matr1 = transform1.GetTransformation();
					
					Vertex v1 = m_vertices.get(m_indices.get(i));
					Vertex v2 = m_vertices.get(m_indices.get(i + 1));
					Vertex v3 = m_vertices.get(m_indices.get(i + 2));
					context.DrawTriangle( v1.Transform(mvp, matrix),
							v2.Transform(mvp.Mul(matr1), matrix.Mul(matr1)),
							v3.Transform(mvp.Mul(matr1), matrix.Mul(matr1)),
							texture);
					down--;
					
				}
			}
			*/
		}
		//System.out.println("min = " + min_v.GetPosition() + "; imp= " +
		//min_v.GetPosition().flat_distance_squared(m_center) + ", but i need " + mfd);
	}
}
