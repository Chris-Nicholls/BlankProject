using System;
using Improbable.Unity.Visualizer;
using Position;
using UnityEngine;

namespace Assets.Gamelogic.Position
{
    class PositionVisualizer2D: MonoBehaviour
    {
        [Require]
        private SimplePosition.Reader Position;

        public void OnEnable()
        {
            SetPosition();
        }

        private void SetPosition()
        {
            transform.position = new Vector3((float) Position.Data.position.X, (float) Position.Data.position.Z, 0);
        }

        public void Update()
        {
            if (!Position.HasAuthority)
            {
                SetPosition();
            }
        }
    }
}
