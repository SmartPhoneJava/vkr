public class Vector
{
	private float x;
	private float y;
	private float z;
	private float w;

	public Vector(float x, float y, float z, float w)
	{
		this.x = x;
		this.y = y;
		this.z = z;
		this.w = w;
	}

	public Vector(float x, float y, float z)
	{
		this(x, y, z, 1.0f);
	}
	
	public float flat_distance_squared(Vector vector)
	{
		return (float)Math.pow(x - vector.x,2) +
				(float)Math.pow(z - vector.z,2);
	}
	
	public float distance_squared(Vector vector)
	{
		return (float)Math.pow(x - vector.x,2) +
				(float)Math.pow(y - vector.y,2) +
				(float)Math.pow(z - vector.z,2);
	}

	public float Length()
	{
		return (float)Math.sqrt(x * x + y * y + z * z + w * w);
	}

	public float Max()
	{
		return Math.max(Math.max(x, y), Math.max(z, w));
	}

	public float Dot(Vector r)
	{
		return x * r.GetX() + y * r.GetY() + z * r.GetZ() + w * r.GetW();
	}

	public Vector Cross(Vector r)
	{
		float x_ = y * r.GetZ() - z * r.GetY();
		float y_ = z * r.GetX() - x * r.GetZ();
		float z_ = x * r.GetY() - y * r.GetX();

		return new Vector(x_, y_, z_, 0);
	}

	public Vector Normalized()
	{
		float length = Length();

		return new Vector(x / length, y / length, z / length, w / length);
	}

	public Vector Rotate(Vector axis, float angle)
	{
		float sinAngle = (float)Math.sin(-angle);
		float cosAngle = (float)Math.cos(-angle);

		return this.Cross(axis.Mul(sinAngle)).Add(           //Rotation on local X
				(this.Mul(cosAngle)).Add(                     //Rotation on local Z
						axis.Mul(this.Dot(axis.Mul(1 - cosAngle))))); //Rotation on local Y
	}

	public Vector Rotate(Quaternion rotation)
	{
		Quaternion conjugate = rotation.Conjugate();

		Quaternion w = rotation.Mul(this).Mul(conjugate);
		/*
		System.out.println("this x:" + this.GetX() +
				   " y:" + this.GetY() +
				   " z:" + this.GetZ() +
				   " w:" + this.GetZ());
		
		System.out.println("conjugate x:" + conjugate.GetX() +
				   " y:" + conjugate.GetY() +
				   " z:" + conjugate.GetZ() +
				   " w:" + conjugate.GetW());
		
		System.out.println("rotation x:" + rotation.GetX() +
				   " y:" + rotation.GetY() +
				   " z:" + rotation.GetZ() +
				   " w:" + rotation.GetW());
				   */

		return new Vector(w.GetX(), w.GetY(), w.GetZ(), 1.0f);
	}

	public Vector Lerp(Vector dest, float lerpFactor)
	{
		return dest.Sub(this).Mul(lerpFactor).Add(this);
	}

	public Vector Add(Vector r)
	{
		return new Vector(x + r.GetX(), y + r.GetY(), z + r.GetZ(), w + r.GetW());
	}

	public Vector Add(float r)
	{
		return new Vector(x + r, y + r, z + r, w + r);
	}

	public Vector Sub(Vector r)
	{
		return new Vector(x - r.GetX(), y - r.GetY(), z - r.GetZ(), w - r.GetW());
	}

	public Vector Sub(float r)
	{
		return new Vector(x - r, y - r, z - r, w - r);
	}

	public Vector Mul(Vector r)
	{
		return new Vector(x * r.GetX(), y * r.GetY(), z * r.GetZ(), w * r.GetW());
	}

	public Vector Mul(float r)
	{
		return new Vector(x * r, y * r, z * r, w * r);
	}

	public Vector Div(Vector r)
	{
		return new Vector(x / r.GetX(), y / r.GetY(), z / r.GetZ(), w / r.GetW());
	}

	public Vector Div(float r)
	{
		return new Vector(x / r, y / r, z / r, w / r);
	}

	public Vector Abs()
	{
		return new Vector(Math.abs(x), Math.abs(y), Math.abs(z), Math.abs(w));
	}

	public String toString()
	{
		return "(" + x + ", " + y + ", " + z + ", " + w + ")";
	}
	
	public void SetX(float x_)
	{
		x = x_;
	}
	
	public void SetY(float y_)
	{
		y = y_;
	}

	public float GetX()
	{
		return x;
	}

	public float GetY()
	{
		return y;
	}

	public float GetZ()
	{
		return z;
	}

	public float GetW()
	{
		return w;
	}

	public boolean equals(Vector r)
	{
		return x == r.GetX() && y == r.GetY() && z == r.GetZ() && w == r.GetW();
	}
}
