public class Camera
{
	private Transform m_transform;
	private Matrix m_projection;

	public Transform GetTransform()
	{
		return m_transform;
	}

	public Camera(Matrix projection)
	{
		this.m_projection = projection;
		this.m_transform = new Transform();
	}

	public Matrix GetViewProjection()
	{
		Matrix cameraRotation = GetTransform().GetTransformedRot().Conjugate().ToRotationMatrix();
		Vector cameraPos = GetTransform().GetTransformedPos().Mul(-1);

		Matrix cameraTranslation = new Matrix().InitTranslation(cameraPos.GetX(), cameraPos.GetY(), cameraPos.GetZ());

		return m_projection.Mul(cameraRotation.Mul(cameraTranslation));
	}

	public void Move(Vector dir, float amt)
	{
		m_transform = GetTransform().SetPos(GetTransform().GetPos().Add(dir.Mul(amt)));
	}

	public void Rotate(Vector axis, float angle)
	{
		m_transform = GetTransform().Rotate(new Quaternion(axis, angle));
	}
}
