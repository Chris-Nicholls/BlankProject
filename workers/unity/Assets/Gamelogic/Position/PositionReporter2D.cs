using Improbable.Math;
using Improbable.Unity.Visualizer;
using Position;
using UnityEngine;

namespace Assets.Gamelogic.Position
{
    public class PositionReporter2D : MonoBehaviour
    {
        [Require] private SimplePosition.Writer Position;

        private SimplePosition.Update _positionUpdate = new SimplePosition.Update();
        private Coordinates _playerCoordinates;

        public void FixedUpdate()
        {
            var position = transform.position;
            _playerCoordinates.X = position.x;
            _playerCoordinates.Z = position.y;
            _positionUpdate.SetPosition(_playerCoordinates);
            Position.Send(_positionUpdate);
        }
    }
}
