public class Quaternion
{
	private float x_;
	private float y_;
	private float z_;
	private float w_;

	public Quaternion(float x, float y, float z, float w)
	{
		this.x_ = x;
		this.y_ = y;
		this.z_ = z;
		this.w_ = w;
	}

	public Quaternion(Vector axis, float angle)
	{
		float sinHalfAngle = (float)Math.sin(angle / 2);
		float cosHalfAngle = (float)Math.cos(angle / 2);

		this.x_ = axis.GetX() * sinHalfAngle;
		this.y_ = axis.GetY() * sinHalfAngle;
		this.z_ = axis.GetZ() * sinHalfAngle;
		this.w_ = cosHalfAngle;
	}

	public float Length()
	{
		return (float)Math.sqrt(x_ * x_ + y_ * y_ + z_ * z_ + w_ * w_);
	}
	
	public Quaternion Normalized()
	{
		float length = Length();
		
		return new Quaternion(x_ / length, y_ / length, z_ / length, w_ / length);
	}
	
	public Quaternion Conjugate()
	{
		return new Quaternion(-x_, -y_, -z_, w_);
	}

	public Quaternion Mul(float r)
	{
		return new Quaternion(x_ * r, y_ * r, z_ * r, w_ * r);
	}

	public Quaternion Mul(Quaternion r)
	{
		float w = w_ * r.GetW() - x_ * r.GetX() - y_ * r.GetY() - z_ * r.GetZ();
		float x = x_ * r.GetW() + w_ * r.GetX() + y_ * r.GetZ() - z_ * r.GetY();
		float y = y_ * r.GetW() + w_ * r.GetY() + z_ * r.GetX() - x_ * r.GetZ();
		float z = z_ * r.GetW() + w_ * r.GetZ() + x_ * r.GetY() - y_ * r.GetX();
		
		return new Quaternion(x, y, z, w);
	}
	
	public Quaternion Mul(Vector r)
	{
		float w = -x_ * r.GetX() - y_ * r.GetY() - z_ * r.GetZ();
		float x =  w_ * r.GetX() + y_ * r.GetZ() - z_ * r.GetY();
		float y =  w_ * r.GetY() + z_ * r.GetX() - x_ * r.GetZ();
		float z =  w_ * r.GetZ() + x_ * r.GetY() - y_ * r.GetX();
		
		return new Quaternion(x, y, z, w);
	}

	public Quaternion Sub(Quaternion r)
	{
		return new Quaternion(x_ - r.GetX(), y_ - r.GetY(), z_ - r.GetZ(), w_ - r.GetW());
	}

	public Quaternion Add(Quaternion r)
	{
		return new Quaternion(x_ + r.GetX(), y_ + r.GetY(), z_ + r.GetZ(), w_ + r.GetW());
	}

	public Matrix ToRotationMatrix()
	{
		Vector forward =  new Vector(2.0f * (x_ * z_ - w_ * y_), 2.0f * (y_ * z_ + w_ * x_), 1.0f - 2.0f * (x_ * x_ + y_ * y_));
		Vector up = new Vector(2.0f * (x_ * y_ + w_ * z_), 1.0f - 2.0f * (x_ * x_ + z_ * z_), 2.0f * (y_ * z_ - w_ * x_));
		Vector right = new Vector(1.0f - 2.0f * (y_ * y_ + z_ * z_), 2.0f * (x_ * y_ - w_ * z_), 2.0f * (x_ * z_ + w_ * y_));

		return new Matrix().InitRotation(forward, up, right);
	}

	public float Dot(Quaternion r)
	{
		return x_ * r.GetX() + y_ * r.GetY() + z_ * r.GetZ() + w_ * r.GetW();
	}

	public Quaternion NLerp(Quaternion dest, float lerpFactor, boolean shortest)
	{
		Quaternion correctedDest = dest;

		if(shortest && this.Dot(dest) < 0)
			correctedDest = new Quaternion(-dest.GetX(), -dest.GetY(), -dest.GetZ(), -dest.GetW());

		return correctedDest.Sub(this).Mul(lerpFactor).Add(this).Normalized();
	}

	public Quaternion SLerp(Quaternion dest, float lerpFactor, boolean shortest)
	{
		final float EPSILON = 1e3f;

		float cos = this.Dot(dest);
		Quaternion correctedDest = dest;

		if(shortest && cos < 0)
		{
			cos = -cos;
			correctedDest = new Quaternion(-dest.GetX(), -dest.GetY(), -dest.GetZ(), -dest.GetW());
		}

		if(Math.abs(cos) >= 1 - EPSILON)
			return NLerp(correctedDest, lerpFactor, false);

		float sin = (float)Math.sqrt(1.0f - cos * cos);
		float angle = (float)Math.atan2(sin, cos);
		float invSin =  1.0f/sin;

		float srcFactor = (float)Math.sin((1.0f - lerpFactor) * angle) * invSin;
		float destFactor = (float)Math.sin((lerpFactor) * angle) * invSin;

		return this.Mul(srcFactor).Add(correctedDest.Mul(destFactor));
	}


	public Quaternion(Matrix rot)
	{
		float trace = rot.Get(0, 0) + rot.Get(1, 1) + rot.Get(2, 2);

		if(trace > 0)
		{
			float s = 0.5f / (float)Math.sqrt(trace+ 1.0f);
			w_ = 0.25f / s;
			x_ = (rot.Get(1, 2) - rot.Get(2, 1)) * s;
			y_ = (rot.Get(2, 0) - rot.Get(0, 2)) * s;
			z_ = (rot.Get(0, 1) - rot.Get(1, 0)) * s;
		}
		else
		{
			if(rot.Get(0, 0) > rot.Get(1, 1) && rot.Get(0, 0) > rot.Get(2, 2))
			{
				float s = 2.0f * (float)Math.sqrt(1.0f + rot.Get(0, 0) - rot.Get(1, 1) - rot.Get(2, 2));
				w_ = (rot.Get(1, 2) - rot.Get(2, 1)) / s;
				x_ = 0.25f * s;
				y_ = (rot.Get(1, 0) + rot.Get(0, 1)) / s;
				z_ = (rot.Get(2, 0) + rot.Get(0, 2)) / s;
			}
			else if(rot.Get(1, 1) > rot.Get(2, 2))
			{
				float s = 2.0f * (float)Math.sqrt(1.0f + rot.Get(1, 1) - rot.Get(0, 0) - rot.Get(2, 2));
				w_ = (rot.Get(2, 0) - rot.Get(0, 2)) / s;
				x_ = (rot.Get(1, 0) + rot.Get(0, 1)) / s;
				y_ = 0.25f * s;
				z_ = (rot.Get(2, 1) + rot.Get(1, 2)) / s;
			}
			else
			{
				float s = 2.0f * (float)Math.sqrt(1.0f + rot.Get(2, 2) - rot.Get(0, 0) - rot.Get(1, 1));
				w_ = (rot.Get(0, 1) - rot.Get(1, 0) ) / s;
				x_ = (rot.Get(2, 0) + rot.Get(0, 2) ) / s;
				y_ = (rot.Get(1, 2) + rot.Get(2, 1) ) / s;
				z_ = 0.25f * s;
			}
		}

		float length = (float)Math.sqrt(x_ * x_ + y_ * y_ + z_ * z_ + w_ * w_);
		x_ /= length;
		y_ /= length;
		z_ /= length;
		w_ /= length;
	}

	public Vector GetForward()
	{
		return new Vector(0,0,1,1).Rotate(this);
	}

	public Vector GetBack()
	{
		return new Vector(0,0,-1,1).Rotate(this);
	}

	public Vector GetUp()
	{
		return new Vector(0,1,0,1).Rotate(this);
	}

	public Vector GetDown()
	{
		return new Vector(0,-1,0,1).Rotate(this);
	}

	public Vector GetRight()
	{
		return new Vector(1,0,0,1).Rotate(this);
	}

	public Vector GetLeft()
	{
		Vector v = new Vector(-1,0,0,1).Rotate(this);
		return new Vector(-1,0,0,1).Rotate(this);
	}
	
	public float GetX()
	{
		return x_;
	}

	public float GetY()
	{
		return y_;
	}

	public float GetZ()
	{
		return z_;
	}

	public float GetW()
	{
		return w_;
	}

	public boolean equals(Quaternion r)
	{
		return x_ == r.GetX() && y_ == r.GetY() && z_ == r.GetZ() && w_ == r.GetW();
	}
}
