using Improbable.Unity.Visualizer;
using UnityEngine;

namespace Position
{
    public class PositionReporter2D : MonoBehaviour
    {
        [Require] private PlayerMovementWriter PlayerMovement;
        
        private void Update()
        {
            var pos = transform.position;
            var convertedPosition = new Improbable.Math.Coordinates(pos.x, 0, pos.y);
            PlayerMovement.Update.Position(convertedPosition).FinishAndSend();
        }
    }
}
