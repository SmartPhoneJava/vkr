
public class Vertex
{
	private Vector m_pos;
	private Vector m_texCoords;
	private Vector m_normal;

	/** Basic Getter */
	public float GetX() { return m_pos.GetX(); }
	/** Basic Getter */
	public float GetY() { return m_pos.GetY(); }
	/** Basic Getter */
	public float GetZ() { return m_pos.GetZ(); }
	
	public void SetPosition(Vector pos) { m_pos = pos; }

	public Vector GetPosition() { return m_pos; }
	public Vector GetTexCoords() { return m_texCoords; }
	public Vector GetNormal() { return m_normal; }

	/**
	 * Creates a new Vertex in a usable state.
	 */
	public Vertex(Vector pos, Vector texCoords, Vector normal)
	{
		m_pos = pos;
		m_texCoords = texCoords;
		m_normal = normal;
	}

	public Vertex Transform(Matrix transform, Matrix normalTransform)
	{
		// The normalized here is important if you're doing scaling.
		return new Vertex(transform.Transform(m_pos), m_texCoords, 
				normalTransform.Transform(m_normal).Normalized());
	}

	public Vertex PerspectiveDivide()
	{
		return new Vertex(new Vector(m_pos.GetX()/m_pos.GetW(), m_pos.GetY()/m_pos.GetW(), 
						m_pos.GetZ()/m_pos.GetW(), m_pos.GetW()),	
				m_texCoords, m_normal);
	}

	public float TriangleAreaTimesTwo(Vertex b, Vertex c)
	{
		float x1 = b.GetX() - m_pos.GetX();
		float y1 = b.GetY() - m_pos.GetY();

		float x2 = c.GetX() - m_pos.GetX();
		float y2 = c.GetY() - m_pos.GetY();

		return (x1 * y2 - x2 * y1);
	}

	public Vertex Lerp(Vertex other, float lerpAmt)
	{
		return new Vertex(
				m_pos.Lerp(other.GetPosition(), lerpAmt),
				m_texCoords.Lerp(other.GetTexCoords(), lerpAmt),
				m_normal.Lerp(other.GetNormal(), lerpAmt)
				);
	}

	public boolean IsInsideViewFrustum()
	{
		return 
			Math.abs(m_pos.GetX()) <= Math.abs(m_pos.GetW()) &&
			Math.abs(m_pos.GetY()) <= Math.abs(m_pos.GetW()) &&
			Math.abs(m_pos.GetZ()) <= Math.abs(m_pos.GetW());
	}

	public float Get(int index)
	{
		switch(index)
		{
			case 0:
				return m_pos.GetX();
			case 1:
				return m_pos.GetY();
			case 2:
				return m_pos.GetZ();
			case 3:
				return m_pos.GetW();
			default:
				throw new IndexOutOfBoundsException();
		}
	}
}
