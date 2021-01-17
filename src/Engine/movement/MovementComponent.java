package Engine.movement;

import com.jme3.ai.navmesh.NavMesh;
import com.jme3.bullet.control.BetterCharacterControl;
import com.jme3.cinematic.MotionPath;
import java.util.function.Function;
import com.jme3.math.Vector3f;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.awt.Rectangle;

import com.jme3.cinematic.MotionPath;
import com.jme3.cinematic.MotionPathListener;
import com.jme3.cinematic.events.MotionEvent;
import com.jme3.math.FastMath;
import com.jme3.math.Quaternion;
import com.jme3.scene.Mesh;
import com.jme3.scene.Spatial;

import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/* This class is a container for many classes which implement some kind of
 * movement algorithm. (think of it as a namespace containing many free functions.
 * To add a new movement algorithm, simply create a new nested class here.
 * It must implement Function<Vector3f, Vector3f>.
 * Too many nested classes? We'll care about that later.
 */
public class MovementComponent extends MotionEvent {

    /*
        Giving this class a Rectangle as such -> this(pos, --new Rectangle(50, 50)--) means that,
        whenever we generate a new point, that point is choosen inside a 100x100 "rectangle".
        As for now we only use rectangles to find new points given a point 'pos'.
        In future implementations we'll see them in use for walls detection and stuff...
     */
    static private final Rectangle DEFAULT_AREA = new Rectangle(50, 50);
    private Vector3f position;
    private final Rectangle area;
    private PollingArea pArea;
    private BetterCharacterControl spatialControl;
    private PathFinder pathFinder;
    
    private PathFollower t;

    private Spatial scene;

    private boolean isWaiting = true;

    public MovementComponent(final Spatial spatial, final Spatial scene, final Vector3f position, final Rectangle area) {
        super(spatial, new MotionPath());
        this.position = position;
        this.area = area;
        this.spatial = spatial;

        this.scene = scene;
        this.pathFinder = new PathFinder(scene);
        t = new PathFollower(spatial, pathFinder, getPointInScene());

        this.spatialControl = spatial.getControl(BetterCharacterControl.class);

        //this.setDirectionType(MotionEvent.Direction.PathAndRotation);
        this.setRotation(new Quaternion().fromAngleNormalAxis(FastMath.PI, Vector3f.UNIT_Y));

        this.setSpeed(300 / path.getLength());
    }

    public MovementComponent(final Spatial spatial, final Spatial scene, final Vector3f position) {
        this(spatial, scene, position, DEFAULT_AREA);
    }

    public Vector3f getPosition() {
        return position;
    }

    public Vector3f getNextPoint() {
        this.pArea = new PollingArea(area, 10);
        this.position = this.position.add(pArea.getRandomOffset());
        return this.position;
    }

    public void moveToNextPointInPath() {
        this.play();
    }

    @Override
    public void onStop() {
        path = null;
        path = new MotionPath();
        path.addWayPoint(this.position);
        path.addWayPoint(new Vector3f(getNextPoint()));
        path.addWayPoint(new Vector3f(getNextPoint()));
        path.addWayPoint(new Vector3f(getNextPoint()));
        this.setPath(path);
        this.setSpeed(300 / path.getLength());
        this.play();
    }

    public MotionPath getPath() {
        return this.path;
    }

    public void moveToTarget(Vector3f target) {
        t.start();
    }

    public void moveDirection(final Vector3f direction) {
        Vector3f v = direction.subtract(spatial.getLocalTranslation());
        spatialControl.setViewDirection(v.negate());
        spatialControl.setWalkDirection(v.normalize().mult(50));
    }

    private boolean stopWalking() {
        if (spatialControl.getWalkDirection() != Vector3f.ZERO) {
            spatialControl.setWalkDirection(Vector3f.ZERO);
            return true;
        }

        return false;
    }

    public Vector3f getPointInScene() {
        return pathFinder.getRandomPoint();
    }

}
