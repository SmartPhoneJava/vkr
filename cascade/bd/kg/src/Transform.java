public class Transform
{
	private Vector   position_;
	public Quaternion rotation_;
	public Vector   scale_;

	public Transform()
	{
		this(new Vector(0,0,0,0));
	}

	public Transform(Vector pos)
	{
		this(pos, new Quaternion(0,0,0,1), new Vector(1,1,1,1));
	}

	public Transform(Vector pos, Quaternion rot, Vector scale)
	{
		position_ = pos;
		rotation_ = rot;
		scale_ = scale;
	}

	public Transform SetPos(Vector pos)
	{
		return new Transform(pos, rotation_, scale_);
	}

	public Transform Rotate(Quaternion rotation)
	{
		return new Transform(position_, rotation.Mul(rotation_).Normalized(), scale_);
	}

	public Transform LookAt(Vector point, Vector up)
	{
		return Rotate(GetLookAtRotation(point, up));
	}

	public Quaternion GetLookAtRotation(Vector point, Vector up)
	{
		return new Quaternion(new Matrix().InitRotation(point.Sub(position_).Normalized(), up));
	}

	public Matrix GetTransformation()
	{
		Matrix translationMatrix = new Matrix().InitTranslation(position_.GetX(), position_.GetY(), position_.GetZ());
		Matrix rotationMatrix = rotation_.ToRotationMatrix();
		Matrix scaleMatrix = new Matrix().InitScale(scale_.GetX(), scale_.GetY(), scale_.GetZ());

		return translationMatrix.Mul(rotationMatrix.Mul(scaleMatrix));
	}

	public Vector GetTransformedPos()
	{
		return position_;
	}

	public Quaternion GetTransformedRot()
	{
		return rotation_;
	}

	public Vector GetPos()
	{
		return position_;
	}

	public Quaternion GetRot()
	{
		return rotation_;
	}

	public Vector GetScale()
	{
		return scale_;
	}
}
