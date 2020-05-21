import java.io.IOException;
import java.awt.Toolkit;
import java.awt.Dimension;

import org.opencv.core.Core;
import org.springframework.boot.SpringApplication;

import static java.lang.Thread.sleep;

public class Main
{
	// Lazy exception handling here. You can do something more interesting 
	// depending on what you're doing
	public static void main(String[] args) throws IOException
	{
		System.loadLibrary(Core.NATIVE_LIBRARY_NAME);
		LoginForm userForm = new LoginForm(false);
		userForm.setVisible(true);

		//long finish = System.currentTimeMillis();
		//long timeConsumedMillis = finish - start;
		//System.out.println("timeConsumedMillis:"+timeConsumedMillis);
	}

	public static void Lauch(AuthToken token, int ProjectID, SceneWithObjects scene) {
		Runnable task = new Runnable() {
			public void run() {
				Dimension screenSize = Toolkit.getDefaultToolkit().getScreenSize();

				int width = screenSize.width;
				int height = screenSize.height - 40;

				TokenProjectScene TPS = new TokenProjectScene(token, ProjectID, scene.scene.ID);
				Display display = new Display(0, 0,width,height, TPS, scene);
				display.allowEryUpdate.lock();
				//display.tryUpdate(token, display.image, ProjectID, scene.scene.ID);
				//MainWindow mainwindow = new MainWindow(display, 0,0,width/2,height);

				RenderContext target = display.GetFrameBuffer();

				Camera camera = new Camera(new Matrix().InitPerspective((float)Math.toRadians(50.0f),
						(float)target.GetWidth()/(float)target.GetHeight(), 0.1f, 10000.0f));
				//camera.Move(camera.GetTransform().GetRot().GetForward(), 4);
				//camera.Rotate(camera.GetTransform().GetRot().GetRight(),  (float) (-Math.PI / 2));
				camera.Move(camera.GetTransform().GetRot().GetForward(), -700);
				camera.Move(camera.GetTransform().GetRot().GetUp(), 350);
				camera.Move(camera.GetTransform().GetRot().GetRight(), 700);
				//camera.Move(camera.GetTransform().GetRot().GetRight(), 800);

				//System.out.println("rotation x:" + camera.GetTransform().GetRot().GetX() +
				//		   " y:" + camera.GetTransform().GetRot().GetY() +
				//		   " z:" + camera.GetTransform().GetRot().GetZ() +
				//		   " w:" + camera.GetTransform().GetRot().GetW());

				//float rotCounter = 0.0f;
				long previousTime = System.nanoTime();

				//monkeyTransform = monkeyTransform.Rotate(new Quaternion(new Vector4f(0,1,0), 3.f));
				int change_z = 0;
				int change_x = 0;
				int change_y = 0;

				do
				{
					long currentTime = System.nanoTime();
					float delta = (float)((currentTime - previousTime)/10000000.0);
					previousTime = currentTime;

					if (display.wantUpdate) {
						break;
					}
					if (display.pause) {
						System.out.println("main unlock!:");
						display.allowEryUpdate.unlock();
						System.out.println("main unlocked!:");
						System.out.println("main lock!:");
						display.allowEryUpdate.lock();
						System.out.println("main locked!:");
						continue;
					}

					//rotCounter += delta;

					Matrix vp = camera.GetViewProjection();

					camera.Move(camera.GetTransform().GetRot().GetForward(), display.change_z - change_z);
					camera.Move(camera.GetTransform().GetRot().GetRight(), display.change_x - change_x);
					camera.Move(camera.GetTransform().GetRot().GetUp(), display.change_y - change_y);
					change_z = display.change_z;
					change_y = display.change_y;
					change_x = display.change_x;

					target.Clear((byte)0xff);
					target.ClearDepthBuffer();
					for (int i = 0; i < display.objs.size(); i++) {
						//System.out.println("display.objs.size()!:"+display.objs.size());
							display.objs.get(i).Draw(target, vp, display.erys.get(i).info.transform,
									display.erys.get(i).texture, display.erys.get(i).info.form,
									display.erys.get(i).info.oxygen,
									(int)(100 * display.erys.get(i).info.gemoglob));

							if (display.erys.get(i).info.correct_gemoglob()) {
								display.erys.get(i).updateGemoglob();
							}
							if (display.erys.get(i).info.correct_form()) {
								display.erys.get(i).updateForm();
							}
							if (display.erys.get(i).info.correct_oxygen()) {
								display.erys.get(i).updateOxygen();
							}
						}
					//}

					display.SwapBuffers();
				}
				while(true);
			}
		};
		Thread thread = new Thread(task);
		thread.start();
	}
}