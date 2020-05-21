public class Matrix
{
	private float[][] m_;

	public Matrix()
	{
		m_ = new float[4][4];
	}

	public Matrix InitIdentity()
	{
		m_[0][0] = 1;	m_[0][1] = 0;	m_[0][2] = 0;	m_[0][3] = 0;
		m_[1][0] = 0;	m_[1][1] = 1;	m_[1][2] = 0;	m_[1][3] = 0;
		m_[2][0] = 0;	m_[2][1] = 0;	m_[2][2] = 1;	m_[2][3] = 0;
		m_[3][0] = 0;	m_[3][1] = 0;	m_[3][2] = 0;	m_[3][3] = 1;

		return this;
	}

	public Matrix InitScreenSpaceTransform(float halfWidth, float halfHeight)
	{
		m_[0][0] = halfWidth;	m_[0][1] = 0;	m_[0][2] = 0;	m_[0][3] = halfWidth - 0.5f;
		m_[1][0] = 0;	m_[1][1] = -halfHeight;	m_[1][2] = 0;	m_[1][3] = halfHeight - 0.5f;
		m_[2][0] = 0;	m_[2][1] = 0;	m_[2][2] = 1;	m_[2][3] = 0;
		m_[3][0] = 0;	m_[3][1] = 0;	m_[3][2] = 0;	m_[3][3] = 1;

		return this;
	}

	public Matrix InitTranslation(float x, float y, float z)
	{
		m_[0][0] = 1;	m_[0][1] = 0;	m_[0][2] = 0;	m_[0][3] = x;
		m_[1][0] = 0;	m_[1][1] = 1;	m_[1][2] = 0;	m_[1][3] = y;
		m_[2][0] = 0;	m_[2][1] = 0;	m_[2][2] = 1;	m_[2][3] = z;
		m_[3][0] = 0;	m_[3][1] = 0;	m_[3][2] = 0;	m_[3][3] = 1;

		return this;
	}

	public Matrix InitRotation(float x, float y, float z, float angle)
	{
		float sin = (float)Math.sin(angle);
		float cos = (float)Math.cos(angle);

		m_[0][0] = cos+x*x*(1-cos); m_[0][1] = x*y*(1-cos)-z*sin; m_[0][2] = x*z*(1-cos)+y*sin; m_[0][3] = 0;
		m_[1][0] = y*x*(1-cos)+z*sin; m_[1][1] = cos+y*y*(1-cos);	m_[1][2] = y*z*(1-cos)-x*sin; m_[1][3] = 0;
		m_[2][0] = z*x*(1-cos)-y*sin; m_[2][1] = z*y*(1-cos)+x*sin; m_[2][2] = cos+z*z*(1-cos); m_[2][3] = 0;
		m_[3][0] = 0;	m_[3][1] = 0;	m_[3][2] = 0;	m_[3][3] = 1;

		return this;
	}

	public Matrix InitRotation(float x, float y, float z)
	{
		Matrix rx = new Matrix();
		Matrix ry = new Matrix();
		Matrix rz = new Matrix();

		rz.m_[0][0] = (float)Math.cos(z);rz.m_[0][1] = -(float)Math.sin(z);rz.m_[0][2] = 0;				rz.m_[0][3] = 0;
		rz.m_[1][0] = (float)Math.sin(z);rz.m_[1][1] = (float)Math.cos(z);rz.m_[1][2] = 0;					rz.m_[1][3] = 0;
		rz.m_[2][0] = 0;					rz.m_[2][1] = 0;					rz.m_[2][2] = 1;					rz.m_[2][3] = 0;
		rz.m_[3][0] = 0;					rz.m_[3][1] = 0;					rz.m_[3][2] = 0;					rz.m_[3][3] = 1;

		rx.m_[0][0] = 1;					rx.m_[0][1] = 0;					rx.m_[0][2] = 0;					rx.m_[0][3] = 0;
		rx.m_[1][0] = 0;					rx.m_[1][1] = (float)Math.cos(x);rx.m_[1][2] = -(float)Math.sin(x);rx.m_[1][3] = 0;
		rx.m_[2][0] = 0;					rx.m_[2][1] = (float)Math.sin(x);rx.m_[2][2] = (float)Math.cos(x);rx.m_[2][3] = 0;
		rx.m_[3][0] = 0;					rx.m_[3][1] = 0;					rx.m_[3][2] = 0;					rx.m_[3][3] = 1;

		ry.m_[0][0] = (float)Math.cos(y);ry.m_[0][1] = 0;					ry.m_[0][2] = -(float)Math.sin(y);ry.m_[0][3] = 0;
		ry.m_[1][0] = 0;					ry.m_[1][1] = 1;					ry.m_[1][2] = 0;					ry.m_[1][3] = 0;
		ry.m_[2][0] = (float)Math.sin(y);ry.m_[2][1] = 0;					ry.m_[2][2] = (float)Math.cos(y);ry.m_[2][3] = 0;
		ry.m_[3][0] = 0;					ry.m_[3][1] = 0;					ry.m_[3][2] = 0;					ry.m_[3][3] = 1;

		m_ = rz.Mul(ry.Mul(rx)).GetM();

		return this;
	}

	public Matrix InitScale(float x, float y, float z)
	{
		m_[0][0] = x;	m_[0][1] = 0;	m_[0][2] = 0;	m_[0][3] = 0;
		m_[1][0] = 0;	m_[1][1] = y;	m_[1][2] = 0;	m_[1][3] = 0;
		m_[2][0] = 0;	m_[2][1] = 0;	m_[2][2] = z;	m_[2][3] = 0;
		m_[3][0] = 0;	m_[3][1] = 0;	m_[3][2] = 0;	m_[3][3] = 1;

		return this;
	}

	public Matrix InitPerspective(float fov, float aspectRatio, float zNear, float zFar)
	{
		float tanHalfFOV = (float)Math.tan(fov / 2);
		float zRange = zNear - zFar;

		m_[0][0] = 1.0f / (tanHalfFOV * aspectRatio);	m_[0][1] = 0;					m_[0][2] = 0;	m_[0][3] = 0;
		m_[1][0] = 0;						m_[1][1] = 1.0f / tanHalfFOV;	m_[1][2] = 0;	m_[1][3] = 0;
		m_[2][0] = 0;						m_[2][1] = 0;					m_[2][2] = (-zNear -zFar)/zRange;	m_[2][3] = 2 * zFar * zNear / zRange;
		m_[3][0] = 0;						m_[3][1] = 0;					m_[3][2] = 1;	m_[3][3] = 0;


		return this;
	}

	public Matrix InitOrthographic(float left, float right, float bottom, float top, float near, float far)
	{
		float width = right - left;
		float height = top - bottom;
		float depth = far - near;

		m_[0][0] = 2/width;m_[0][1] = 0;	m_[0][2] = 0;	m_[0][3] = -(right + left)/width;
		m_[1][0] = 0;	m_[1][1] = 2/height;m_[1][2] = 0;	m_[1][3] = -(top + bottom)/height;
		m_[2][0] = 0;	m_[2][1] = 0;	m_[2][2] = -2/depth;m_[2][3] = -(far + near)/depth;
		m_[3][0] = 0;	m_[3][1] = 0;	m_[3][2] = 0;	m_[3][3] = 1;

		return this;
	}

	public Matrix InitRotation(Vector forward, Vector up)
	{
		Vector f = forward.Normalized();

		Vector r = up.Normalized();
		r = r.Cross(f);

		Vector u = f.Cross(r);

		return InitRotation(f, u, r);
	}

	public Matrix InitRotation(Vector forward, Vector up, Vector right)
	{
		Vector f = forward;
		Vector r = right;
		Vector u = up;

		m_[0][0] = r.GetX();	m_[0][1] = r.GetY();	m_[0][2] = r.GetZ();	m_[0][3] = 0;
		m_[1][0] = u.GetX();	m_[1][1] = u.GetY();	m_[1][2] = u.GetZ();	m_[1][3] = 0;
		m_[2][0] = f.GetX();	m_[2][1] = f.GetY();	m_[2][2] = f.GetZ();	m_[2][3] = 0;
		m_[3][0] = 0;		m_[3][1] = 0;		m_[3][2] = 0;		m_[3][3] = 1;

		return this;
	}

	public Vector Transform(Vector r)
	{
		return new Vector(m_[0][0] * r.GetX() + m_[0][1] * r.GetY() + m_[0][2] * r.GetZ() + m_[0][3] * r.GetW(),
		                    m_[1][0] * r.GetX() + m_[1][1] * r.GetY() + m_[1][2] * r.GetZ() + m_[1][3] * r.GetW(),
		                    m_[2][0] * r.GetX() + m_[2][1] * r.GetY() + m_[2][2] * r.GetZ() + m_[2][3] * r.GetW(),
							m_[3][0] * r.GetX() + m_[3][1] * r.GetY() + m_[3][2] * r.GetZ() + m_[3][3] * r.GetW());
	}

	public Matrix Mul(Matrix r)
	{
		Matrix res = new Matrix();

		for(int i = 0; i < 4; i++)
		{
			for(int j = 0; j < 4; j++)
			{
				res.Set(i, j, m_[i][0] * r.Get(0, j) +
						m_[i][1] * r.Get(1, j) +
						m_[i][2] * r.Get(2, j) +
						m_[i][3] * r.Get(3, j));
			}
		}

		return res;
	}

	public float[][] GetM()
	{
		float[][] res = new float[4][4];

		for(int i = 0; i < 4; i++)
			for(int j = 0; j < 4; j++)
				res[i][j] = m_[i][j];

		return res;
	}

	public float Get(int x, int y)
	{
		return m_[x][y];
	}

	public void SetM(float[][] m)
	{
		this.m_ = m;
	}

	public void Set(int x, int y, float value)
	{
		m_[x][y] = value;
	}
}
